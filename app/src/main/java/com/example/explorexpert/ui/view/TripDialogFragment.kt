package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.explorexpert.R
import com.example.explorexpert.adapters.SavedItemAdapter
import com.example.explorexpert.adapters.TripAdapter
import com.example.explorexpert.adapters.observers.ScrollToTopObserver
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.DialogTripBinding
import com.example.explorexpert.databinding.FragmentPlanBinding
import com.example.explorexpert.ui.viewmodel.CreateTripViewModel
import com.example.explorexpert.ui.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TripDialogFragment(
    private val trip: Trip
) : DialogFragment() {

    @Inject
    lateinit var tripViewModel: TripViewModel

    private lateinit var adapter: SavedItemAdapter

    private var _binding: DialogTripBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FullScreenDialogStyle)

        tripViewModel.setTrip(trip)
        tripViewModel.fetchSavedItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = DialogTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showProgressIndicator()

        configureRecyclerView()
        configureButtons()
//        configureObservers()
    }

    private fun configureButtons() {
        binding.btnBackIcon.setOnClickListener {
            this.dismiss()
        }
    }

    private fun configureRecyclerView() {
        adapter = SavedItemAdapter(object : SavedItemAdapter.ItemClickListener {
            override fun onItemClick(savedItem: SavedItem) {
                // Summon dialog for showing saved item details
//                val tripDialogFragment = TripDialogFragment(trip)
//                tripDialogFragment.show(
//                    childFragmentManager,
//                    "tripDialog"
//                )
            }
        })
        binding.savedItemsRecyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(
            ScrollToTopObserver(binding.savedItemsRecyclerView)
        )
    }

    private fun showProgressIndicator() {
        binding.progressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        binding.progressIndicator.visibility = View.INVISIBLE
    }
}