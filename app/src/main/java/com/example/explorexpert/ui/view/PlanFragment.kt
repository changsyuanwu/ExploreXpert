package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.explorexpert.MainActivity
import com.example.explorexpert.R
import com.example.explorexpert.adapters.SavedItemAdapter
import com.example.explorexpert.adapters.TripAdapter
import com.example.explorexpert.adapters.observers.ScrollToTopObserver
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.databinding.FragmentPlanBinding
import com.example.explorexpert.ui.viewmodel.PlanViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class PlanFragment : Fragment() {

    companion object {
        private val TAG = "PlanFragment"
    }

    @Inject
    lateinit var planViewModel: PlanViewModel

    @Inject
    lateinit var tripRepo: TripRepository

    private var _binding: FragmentPlanBinding? = null

    private lateinit var tripAdapter: TripAdapter
    private lateinit var savedItemAdapter: SavedItemAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Stub for grabbing info from map fragment, can move to other parts of module
        if (isAdded) {
            println((requireActivity() as MainActivity).getMapFragment().getMarkedAddress())
        }

        showProgressIndicator()

        configureTripsRecyclerView()
        configureSavedItemsRecyclerView()
        configureButtons()
        configureObservers()
    }

    private fun configureButtons() {
        binding.btnCreateATrip.setOnClickListener {
            val createTripBottomSheetDialogFragment = CreateTripBottomSheetDialogFragment()
            createTripBottomSheetDialogFragment.show(
                childFragmentManager,
                CreateTripBottomSheetDialogFragment.TAG
            )
        }
    }

    private fun configureTripsRecyclerView() {
        tripAdapter = TripAdapter(
            tripRepo,
            object : TripAdapter.ItemClickListener {
                override fun onItemClick(trip: Trip) {
                    // Summon dialog for viewing trip saved items
                    val tripDialogFragment = TripDialogFragment(trip)
                    tripDialogFragment.show(
                        childFragmentManager,
                        TripDialogFragment.TAG
                    )
                }
            }
        )

        binding.tripRecyclerView.adapter = tripAdapter

        val tripLayoutManager = LinearLayoutManager(requireContext())
        binding.tripRecyclerView.layoutManager = tripLayoutManager

        tripAdapter.registerAdapterDataObserver(
            ScrollToTopObserver(binding.tripRecyclerView)
        )

        // Pad the bottom of the trip recycler view so we can scroll past the "create a trip" button
        val tripRecyclerViewBottomPadding =
            binding.btnCreateATrip.height + dpToPixels(binding.btnCreateATrip.marginBottom) + dpToPixels(
                34
            )
        binding.tripRecyclerView.updatePadding(bottom = tripRecyclerViewBottomPadding)
    }

    private fun configureSavedItemsRecyclerView() {
        savedItemAdapter = SavedItemAdapter(
            isInTripDialog = false,
            itemClickListener =  object : SavedItemAdapter.ItemClickListener {
                override fun onItemClick(savedItem: SavedItem) {
                    // Summon dialog for showing saved item details
//                val tripDialogFragment = TripDialogFragment(trip)
//                tripDialogFragment.show(
//                    childFragmentManager,
//                    "tripDialog"
//                )
                }
            },
            tripRepo = tripRepo,
            currentUserId = planViewModel.getCurrentUserId()
        )

        binding.savedItemsRecyclerView.adapter = savedItemAdapter

        val savedItemLayoutManager =LinearLayoutManager(requireContext())
        binding.savedItemsRecyclerView.layoutManager = savedItemLayoutManager

        savedItemAdapter.registerAdapterDataObserver(
            ScrollToTopObserver(binding.savedItemsRecyclerView)
        )
    }

    private fun dpToPixels(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun configureObservers() {
        planViewModel.trips.observe(viewLifecycleOwner) { trips ->
            tripAdapter.submitList(trips)
            hideProgressIndicator()
        }

        planViewModel.savedItems.observe(viewLifecycleOwner) { savedItems ->
            savedItemAdapter.submitList(savedItems)
            hideProgressIndicator()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    getString(R.string.trips) -> {
                        showProgressIndicator()
                        planViewModel.fetchTrips()
                        binding.tripRecyclerView.visibility = View.VISIBLE
                        binding.btnCreateATrip.visibility = View.VISIBLE
                        binding.savedItemsRecyclerView.visibility = View.GONE
                    }

                    getString(R.string.saved_items) -> {
                        showProgressIndicator()
                        planViewModel.fetchSavedItems()
                        binding.tripRecyclerView.visibility = View.GONE
                        binding.btnCreateATrip.visibility = View.GONE
                        binding.savedItemsRecyclerView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun showProgressIndicator() {
        binding.progressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        binding.progressIndicator.visibility = View.INVISIBLE
    }

    fun refreshRecyclerViews() {
        if (this::planViewModel.isInitialized) {
            planViewModel.fetchTrips()
            planViewModel.fetchSavedItems()
        }
    }

    fun scheduleRecyclerViewRefresh() {
        val timer = Timer()
        var executionCount = 0

        timer.scheduleAtFixedRate(
            timerTask {
                if (executionCount > 5) {
                    this.cancel()
                }
                executionCount++
                refreshRecyclerViews()
            },
            300,
            1000
        )
    }
}