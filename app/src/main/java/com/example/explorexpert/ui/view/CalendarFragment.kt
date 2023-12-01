package com.example.explorexpert.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.R
import com.example.explorexpert.adapters.EventAdapter
import com.example.explorexpert.adapters.SavedItemAdapter
import com.example.explorexpert.adapters.TripAdapter
import com.example.explorexpert.adapters.observers.ScrollToTopObserver
import com.example.explorexpert.data.implementation.EventRepoImplementation
import com.example.explorexpert.data.model.Event
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.EventRepository
import com.example.explorexpert.databinding.FragmentCalendarBinding
import com.example.explorexpert.databinding.FragmentPlanBinding
import com.example.explorexpert.ui.viewmodel.CalendarViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.selects.select
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

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
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        selectedDate = LocalDateTime.now().format(formatter)
        binding.dateView.text = selectedDate

        // initialize current date list
        calendarViewModel.fetchEventsByStartDate(selectedDate)

        configureEventsRecyclerView()
        configureObservers()
        configureButtons()
    }

    private fun configureEventsRecyclerView() {
        val eventLayoutManager = LinearLayoutManager(requireContext())
        binding.eventRecyclerView.layoutManager = eventLayoutManager

    }
    private fun configureObservers() {
        calendarViewModel.events.observe(viewLifecycleOwner) { events ->
            binding.eventRecyclerView.adapter = EventAdapter(events)
        }
    }

    private fun configureButtons() {
        binding.btnEventAdd.setOnClickListener {
            val eventName = binding.etEventInput.text.toString()
            calendarViewModel.createEvent(eventName, selectedDate)
            calendarViewModel.fetchEventsByStartDate(selectedDate)
            binding.etEventInput.text = null
        }

        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            selectedDate = (year.toString() + "-" + (month + 1) + "-" + dayOfMonth)
            binding.dateView.text = selectedDate
            calendarViewModel.fetchEventsByStartDate(selectedDate)
        }
    }

}