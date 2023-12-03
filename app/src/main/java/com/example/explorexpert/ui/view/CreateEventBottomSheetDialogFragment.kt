package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.explorexpert.data.model.DateTimeRange
import com.example.explorexpert.databinding.CreateEventBottomSheetBinding
import com.example.explorexpert.ui.viewmodel.CalendarViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class CreateEventBottomSheetDialogFragment(
    private var selectedDateOnCalendar: String
): BottomSheetDialogFragment() {

    @Inject
    lateinit var calendarViewModel: CalendarViewModel

    private var _binding: CreateEventBottomSheetBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectedDateTimeRange: DateTimeRange
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = CreateEventBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureButtons()
    }

    private fun configureButtons() {
        binding.btnBottomSheetCreateEvent.setOnClickListener {
            val eventName = binding.txtInputEventName.editText?.text.toString()

            if (eventName == "") {
                binding.txtInputEventName.error = "Event name cannot be empty"
                return@setOnClickListener
            }
            else if (!this::selectedDateTimeRange.isInitialized) {
                binding.txtInputEventName.error = "Event dates are not specified"
                return@setOnClickListener
            }
            else {
                binding.txtInputEventName.error = null
            }

            // convert to time string
            val startDate = convertLongToDateString(selectedDateTimeRange.startTime)
            val endDate = convertLongToDateString(selectedDateTimeRange.endTime)

            // create events
            calendarViewModel.createEvent(eventName, startDate, endDate)

            val parentFrag = requireParentFragment()
            Snackbar.make(
                parentFrag.view as View,
                "Successfully created $eventName",
                Snackbar.LENGTH_SHORT
            ).show()

            refreshParent()

            this.dismiss()
        }

        configureAddDatesButton()

    }

    private fun configureAddDatesButton() {
        binding.btnAddEventDates.setOnClickListener(null)

        val dateRangePickerBuilder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Add dates")

        // initialize start date to the date selected on the calendar
        val androidxDateSelectedPair = androidx.core.util.Pair<Long, Long>(
            convertDateStringToLong(selectedDateOnCalendar),
            null
        )
        dateRangePickerBuilder.setSelection(androidxDateSelectedPair)


        val dateRangePicker = dateRangePickerBuilder.build()

        dateRangePicker.addOnPositiveButtonClickListener {
            if (dateRangePicker.selection != null) {
                selectedDateTimeRange = DateTimeRange(
                    dateRangePicker.selection!!.first,
                    dateRangePicker.selection!!.second
                )
            }
        }

        binding.btnAddEventDates.setOnClickListener {
            dateRangePicker.show(parentFragmentManager, "tripDateRangePicker")
        }

    }
    private fun convertDateStringToLong(date: String): Long {
        val df = SimpleDateFormat("yyyy-MM-dd")
        return df.parse(date).time
    }

    private fun convertLongToDateString(long: Long): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(long)
                .plus(1, ChronoUnit.DAYS),
            ZoneId.systemDefault()
        )
        return dateTime.format(dateTimeFormatter)
    }

    private fun refreshParent() {
        (requireParentFragment() as CalendarFragment).refreshRecyclerViews()
        (requireParentFragment() as CalendarFragment).scheduleRecyclerViewsRefresh()
    }

    companion object {
        const val TAG: String = "CreateEventBottomSheetDialogFragment"
    }

}