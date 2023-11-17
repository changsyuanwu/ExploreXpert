package com.example.explorexpert.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.explorexpert.MainActivity
import com.example.explorexpert.R
import com.example.explorexpert.adapters.TripAdapter
import com.example.explorexpert.adapters.observers.ScrollToTopObserver
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.FragmentPlanBinding
import com.example.explorexpert.ui.viewmodel.PlanViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlanFragment : Fragment() {

    companion object {
        private val TAG = "PlanFragment"
    }

    @Inject
    lateinit var planViewModel: PlanViewModel

    private var _binding: FragmentPlanBinding? = null

    private lateinit var adapter: TripAdapter

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

        configureRecyclerView()
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

    private fun configureRecyclerView() {
        adapter = TripAdapter(object : TripAdapter.ItemClickListener {
            override fun onItemClick(trip: Trip) {
                // Summon dialog for viewing trip saved items
                val tripDialogFragment = TripDialogFragment(trip)
                tripDialogFragment.show(
                    childFragmentManager,
                    "tripDialog"
                )
            }
        })

        binding.tripRecyclerView.adapter = adapter

        val tripLayoutManager = LinearLayoutManager(requireContext())
        binding.tripRecyclerView.layoutManager = tripLayoutManager


        adapter.registerAdapterDataObserver(
            ScrollToTopObserver(binding.tripRecyclerView)
        )

        // Pad the bottom of the trip recycler view so we can scroll past the "create a trip" button
        val tripRecyclerViewBottomPadding =
            binding.btnCreateATrip.height + dpToPixels(binding.btnCreateATrip.marginBottom) + dpToPixels(34)
        binding.tripRecyclerView.updatePadding(bottom = tripRecyclerViewBottomPadding)
    }

    private fun dpToPixels(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun configureObservers() {
        planViewModel.trips.observe(viewLifecycleOwner) { trips ->
            adapter.submitList(trips)
            hideProgressIndicator()
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    getString(R.string.trips) -> {
                        showProgressIndicator()
                        planViewModel.fetchTrips()
                        hideProgressIndicator()
                    }
                    getString(R.string.saved_items) -> {
                        showProgressIndicator()
                        hideProgressIndicator()
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
}