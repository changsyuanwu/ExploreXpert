package com.example.explorexpert.ui.view

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.explorexpert.R
import com.example.explorexpert.adapters.SavedItemAdapter
import com.example.explorexpert.adapters.observers.ScrollToTopObserver
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.DialogTripBinding
import com.example.explorexpert.ui.viewmodel.AddTripItemViewModel
import com.example.explorexpert.ui.viewmodel.TripViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TripDialogFragment(
    private var trip: Trip
) : DialogFragment() {

    @Inject
    lateinit var tripViewModel: TripViewModel
    @Inject
    lateinit var addTripItemViewModel: AddTripItemViewModel

    private lateinit var adapter: SavedItemAdapter

    private var _binding: DialogTripBinding? = null

    private val binding get() = _binding!!

    private lateinit var appInfo: ApplicationInfo

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appInfo = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FullScreenDialogSlideLeftStyle)

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

        configureUI()
        configureRecyclerView()
        configurePlacesSDK()
        configureButtons()
        configureObservers()
    }

    private fun configureUI() {
        binding.txtTripTitle.text = trip.name

        if (trip.savedItemIds.size == 1) {
            binding.txtNumItems.text = "1 item"
        }
        else {
            binding.txtNumItems.text = "${trip.savedItemIds.size} items"
        }

        CoroutineScope(Dispatchers.Main).launch {
            binding.txtOwner.text = "By ${tripViewModel.getOwnerUserName(trip.ownerUserId)}"
        }
    }

    private fun configureButtons() {
        binding.btnBackIcon.setOnClickListener {
            this.dismiss()
        }

        binding.fabAddNote.setOnClickListener {
            val addNoteBottomSheetDialogFragment = AddNoteBottomSheetDialogFragment(trip)
            addNoteBottomSheetDialogFragment.show(
                childFragmentManager,
                AddNoteBottomSheetDialogFragment.TAG
            )
        }

        binding.fabAddPlace.setOnClickListener {
            startPlaceSearch()
//            val addPlaceDialogFragment = AddPlaceDialogFragment(trip)
//            addPlaceDialogFragment.show(
//                childFragmentManager,
//                AddPlaceDialogFragment.TAG
//            )
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

        val itemsLayoutManager = LinearLayoutManager(requireContext())
        binding.savedItemsRecyclerView.layoutManager = itemsLayoutManager

        adapter.registerAdapterDataObserver(
            ScrollToTopObserver(binding.savedItemsRecyclerView)
        )

        val verticalItemDivider = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        binding.savedItemsRecyclerView.addItemDecoration(verticalItemDivider)
    }

    private fun configureObservers() {
        tripViewModel.savedItems.observe(viewLifecycleOwner) { savedItems ->
            adapter.submitList(savedItems)
            hideProgressIndicator()
            Log.d(TAG, savedItems.toString())
        }
    }

    private fun configurePlacesSDK() {
        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), appId);
        }
        val placesClient = Places.createClient(requireContext())
    }

    private fun startPlaceSearch() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS
        )

        val roughBoundForCanada = RectangularBounds.newInstance(
            LatLng(40.00, -141.00),
            LatLng(85.00, -50.00)
        )

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
            .setLocationBias(roughBoundForCanada)
            .build(context)
        startAutocomplete.launch(intent)
    }


    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    Log.i(
                        TAG, "Place: ${place.name}, ${place.id}"
                    )
                    addTripItemViewModel.addPlace(place)
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(TAG, "User canceled autocomplete")
            } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
                // Handle Autocomplete error
                val status = Autocomplete.getStatusFromIntent(result.data)
                Log.e(TAG, "Error during autocomplete: ${status.statusMessage}")
            }
        }

    fun refreshTrip() {
        tripViewModel.refreshTrip()
    }

    private fun showProgressIndicator() {
        binding.progressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        binding.progressIndicator.visibility = View.INVISIBLE
    }

    companion object {
        const val TAG: String = "TripDialogFragment"
    }
}