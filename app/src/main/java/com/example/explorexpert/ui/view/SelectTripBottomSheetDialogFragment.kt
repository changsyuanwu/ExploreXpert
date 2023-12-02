package com.example.explorexpert.ui.view

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.explorexpert.MainActivity
import com.example.explorexpert.adapters.SimpleTripAdapter
import com.example.explorexpert.adapters.observers.ScrollToTopObserver
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.databinding.SelectTripBottomSheetBinding
import com.example.explorexpert.ui.viewmodel.AddTripItemViewModel
import com.example.explorexpert.ui.viewmodel.PlanViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SelectTripBottomSheetDialogFragment(
    private val isCreatedFromPlaceRecommendedOnHome: Boolean = false,
    private val isCreatedFromNonOwnerViewingSavedItem: Boolean = false,
    private val placeToAdd: Place? = null,
    private val savedItemToCopy: SavedItem? = null,
) : BottomSheetDialogFragment() {

    private var _binding: SelectTripBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var simpleTripAdapter: SimpleTripAdapter

    @Inject
    lateinit var addTripItemViewModel: AddTripItemViewModel

    @Inject
    lateinit var planViewModel: PlanViewModel

    @Inject
    lateinit var tripRepo: TripRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SelectTripBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureRecyclerView()
        configureObservers()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (!isCreatedFromPlaceRecommendedOnHome && !isCreatedFromNonOwnerViewingSavedItem) {
            (parentFragment as LocationBottomSheetDialogFragment).dismiss()
        }
    }

    private fun showAddToTripSuccessSnackbar(tripName: String) {
        val parentFrag = requireParentFragment()
        Snackbar.make(
            parentFrag.view as View,
            "Successfully added to $tripName",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun configureRecyclerView() {
        simpleTripAdapter = SimpleTripAdapter(
            tripRepo,
            object : SimpleTripAdapter.ItemClickListener {
                override fun onItemClick(trip: Trip) {
                    // Set the trip to the one that was clicked on
                    addTripItemViewModel.setTrip(trip)

                    if (isCreatedFromPlaceRecommendedOnHome) {
                        if (placeToAdd != null) {
                            addTripItemViewModel.addPlace(placeToAdd)

                            showAddToTripSuccessSnackbar(trip.name)
                        }
                    }
                    else if (isCreatedFromNonOwnerViewingSavedItem) {
                        if (savedItemToCopy != null) {
                            addTripItemViewModel.addCopyOfSavedItem(savedItemToCopy)

                            showAddToTripSuccessSnackbar(trip.name)
                        }
                    }
                    else {
                        addPlaceToTripWithID(
                            (requireActivity() as MainActivity).getMapFragment().getCurrPlaceID()
                        )
                        (parentFragment as LocationBottomSheetDialogFragment).setTripAdded()
                    }
                    dismiss()
                }
            }
        )

        binding.tripsRecyclerView.adapter = simpleTripAdapter

        val tripLayoutManager = LinearLayoutManager(requireContext())
        binding.tripsRecyclerView.layoutManager = tripLayoutManager

        simpleTripAdapter.registerAdapterDataObserver(
            ScrollToTopObserver(binding.tripsRecyclerView)
        )
    }

    private fun configureObservers() {
        planViewModel.trips.observe(viewLifecycleOwner) { trips ->
            simpleTripAdapter.submitList(trips)
        }
    }

    private fun addPlaceToTripWithID(placeId: String) {
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)
        val placesClient = Places.createClient(requireContext())

        val request = FetchPlaceRequest.newInstance(placeId, placeFields)
        placesClient.fetchPlace(request).addOnSuccessListener { response: FetchPlaceResponse ->
            addTripItemViewModel.addPlace(response.place)
        }
    }

    companion object {
        const val TAG: String = "SelectTripBottomSheetDialogFragment"
    }

}
