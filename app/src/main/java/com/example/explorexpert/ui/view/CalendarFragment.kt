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
import com.example.explorexpert.adapters.EventAdapter
import com.example.explorexpert.data.repository.EventRepository
import com.example.explorexpert.databinding.FragmentCalendarBinding
import com.example.explorexpert.ui.viewmodel.CalendarViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class CalendarFragment : Fragment() {

    companion object {
        private val TAG = "CalendarFragment"
    }

    @Inject
    lateinit var calendarViewModel: CalendarViewModel
    @Inject
    lateinit var eventRepo: EventRepository

    private var _binding: FragmentCalendarBinding? = null
    private lateinit var eventAdapter: EventAdapter

    private val binding get() = _binding!!

    private lateinit var selectedDate: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize to current date
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        selectedDate = LocalDateTime.now().format(formatter)
        binding.dateView.text = selectedDate

        // display current date
        var englishFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
        binding.dateView.text = LocalDateTime.now().format(englishFormatter)

        // initialize current date list
        calendarViewModel.fetchEventsByDate(selectedDate)

        configureEventsRecyclerView()
        configureObservers()
        configureButtons()
    }

    private fun configureEventsRecyclerView() {
        val eventLayoutManager = LinearLayoutManager(requireContext())
        binding.eventRecyclerView.layoutManager = eventLayoutManager

        // Pad the bottom of the event recycler view so we can scroll past the "create a event" button (from PlanFragment)
        val eventRecyclerViewBottomPadding =
            binding.btnCreateAnEvent.height + dpToPixels(binding.btnCreateAnEvent.marginBottom) + dpToPixels(
                34
            )
        binding.eventRecyclerView.updatePadding(bottom = eventRecyclerViewBottomPadding)
    }
    private fun configureObservers() {
        calendarViewModel.events.observe(viewLifecycleOwner) { events ->
            binding.eventRecyclerView.adapter = EventAdapter(events)
        }
    }

    private fun configureButtons() {
        binding.btnCreateAnEvent.setOnClickListener {
            val createEventBottomSheetDialogFragment = CreateEventBottomSheetDialogFragment(selectedDate)
            createEventBottomSheetDialogFragment.show(
                childFragmentManager,
                CreateEventBottomSheetDialogFragment.TAG
            )
        }

        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // pad 0s
            val monthStr = if (month < 10) "0${month+1}" else month+1
            val dayStr = if (dayOfMonth < 10) "0${dayOfMonth}" else dayOfMonth
            selectedDate = ("$year/$monthStr/$dayStr")

            // display date as english
            val englishFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)
            var dateString = ("$year-$monthStr-$dayStr") + "T00:00:00.000Z"
            binding.dateView.text = englishFormatter.format(OffsetDateTime.parse(dateString))

            calendarViewModel.fetchEventsByDate(selectedDate)
        }
    }

    private fun displayEnglishDate(year: String, month: String, day: String) {
    }


    fun refreshRecyclerViews() {
        if (this::calendarViewModel.isInitialized) {
            calendarViewModel.fetchEventsByDate(selectedDate)
        }
    }

    fun scheduleRecyclerViewsRefresh() {
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

    private fun dpToPixels(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

}