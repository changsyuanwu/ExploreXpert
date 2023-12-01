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
import com.example.explorexpert.data.model.DateTimeRange
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.databinding.DialogTripBinding
import com.example.explorexpert.ui.viewmodel.AddTripItemViewModel
import com.example.explorexpert.ui.viewmodel.TripViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class TripDialogFragment(
    private var trip: Trip
) : DialogFragment() {

    @Inject
    lateinit var tripViewModel: TripViewModel

    @Inject
    lateinit var addTripItemViewModel: AddTripItemViewModel

    @Inject
    lateinit var tripRepo: TripRepository

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
        addTripItemViewModel.setTrip(trip)
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

        configureUI(trip)
        configureRecyclerView()
        configurePlacesSDK()
        configureButtons()
        configureObservers()
    }

    private fun configureUI(tripToUse: Trip) {
        binding.txtTripTitle.text = tripToUse.name

        configureSavedItemsCount(tripToUse.savedItemIds.size)
        configureSelectedDates(tripToUse.datesSelected)
        configureAddDatesButtonListener()

        CoroutineScope(Dispatchers.Main).launch {
            binding.txtOwner.text = "By ${tripViewModel.getOwnerUserName(tripToUse.ownerUserId)}"
        }
    }

    private fun configureSavedItemsCount(savedItemCount: Int) {
        if (savedItemCount == 1) {
            binding.txtNumItems.text = "1 item"
        } else {
            binding.txtNumItems.text = "$savedItemCount items"
        }
    }

    private fun configureSelectedDates(selectedDates: DateTimeRange?) {
        if (selectedDates != null) {
            val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd")

            val startDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(selectedDates.startTime)
                    .plus(1, ChronoUnit.DAYS),
                ZoneId.systemDefault()
            )
            val startDate = startDateTime.format(dateTimeFormatter)

            val endDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(selectedDates.endTime)
                    .plus(1, ChronoUnit.DAYS),
                ZoneId.systemDefault()
            )
            val endDate = endDateTime.format(dateTimeFormatter)

            binding.btnAddDates.text = "$startDate ➡ $endDate"
        }
        else {
            binding.btnAddDates.text = "Add trip dates"
        }
    }

    private fun configureButtons() {
        binding.btnBackIcon.setOnClickListener {
            (requireParentFragment() as PlanFragment).refreshRecyclerViews()
            (requireParentFragment() as PlanFragment).scheduleRecyclerViewsRefresh()
            this.dismiss()
        }

        binding.btnRefreshIcon.setOnClickListener {
            refreshTrip()
        }

        binding.btnEditIcon.setOnClickListener {
            val editTripDialogFragment = EditTripDialogFragment(trip)
            editTripDialogFragment.show(
                childFragmentManager,
                "editTripDialog"
            )
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
        }

        // Need to find library to extract details from URL, current one doesnt work
//        binding.fabAddLink.setOnClickListener {
//            val addLinkBottomSheetDialogFragment = AddLinkBottomSheetDialogFragment(trip)
//            addLinkBottomSheetDialogFragment.show(
//                childFragmentManager,
//                AddLinkBottomSheetDialogFragment.TAG
//            )
//        }
    }

    private fun configureAddDatesButtonListener() {
        binding.btnAddDates.setOnClickListener(null)

        val dateRangePickerBuilder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Add dates")

        if (trip.datesSelected != null) {
            val androidxDateSelectedPair = androidx.core.util.Pair<Long, Long>(
                trip.datesSelected!!.startTime,
                trip.datesSelected!!.endTime
            )
            dateRangePickerBuilder.setSelection(androidxDateSelectedPair)
        }

        val dateRangePicker = dateRangePickerBuilder.build()

        dateRangePicker.addOnPositiveButtonClickListener {
            if (dateRangePicker.selection != null) {
                val selectedDateTimeRange = DateTimeRange(
                    dateRangePicker.selection!!.first,
                    dateRangePicker.selection!!.second
                )
                configureSelectedDates(selectedDateTimeRange)
                tripViewModel.updateTripDates(selectedDateTimeRange)
                refreshTripNowAndLater()
            }
        }

        binding.btnAddDates.setOnClickListener {
            dateRangePicker.show(parentFragmentManager, "tripDateRangePicker")
        }
    }

    private fun configureRecyclerView() {
        adapter = SavedItemAdapter(
            isInTripDialog = true,
            itemClickListener = object : SavedItemAdapter.ItemClickListener {
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
            trip = trip,
            currentUserId = tripViewModel.getCurrentUserId()
        )
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
            configureSavedItemsCount(savedItems.size)
            hideProgressIndicator()
        }

        tripViewModel.trip.observe(viewLifecycleOwner) {
            configureUI(it)
            addTripItemViewModel.setTrip(it)
            trip = it
        }
    }

    private fun configurePlacesSDK() {
        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), appId)
        }
        val placesClient = Places.createClient(requireContext())
    }

    private fun startPlaceSearch() {
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
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
                    refreshTripNowAndLater()
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

    private fun refreshTripNowAndLater() {
        refreshTrip()
        scheduleTripRefresh()
    }


    fun refreshTrip() {
        tripViewModel.refreshTrip()
    }

    fun scheduleTripRefresh() {
        val timer = Timer()
        var executionCount = 0

        timer.scheduleAtFixedRate(
            timerTask {
                if (executionCount > 5) {
                    this.cancel()
                }
                executionCount++
                refreshTrip()
            },
            300,
            1000
        )
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