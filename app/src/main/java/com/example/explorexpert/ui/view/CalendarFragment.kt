package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.R
import com.example.explorexpert.adapters.EventAdapter
import com.example.explorexpert.data.model.Event
import com.example.explorexpert.data.repository.EventRepository
import com.example.explorexpert.databinding.FragmentCalendarBinding
import com.example.explorexpert.ui.viewmodel.CalendarViewModel
import dagger.hilt.android.AndroidEntryPoint
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


    lateinit var dateTV: TextView
    lateinit var calendarView: CalendarView
    lateinit var addBtn: Button
    lateinit var eventRecyclerView: RecyclerView
    val allCalendarEvents = mutableMapOf<String, ArrayList<Event>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        calendarViewModel.fetchEvents()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)


        // initialize calendar here

        eventRecyclerView = view.findViewById(R.id.eventRecyclerView)

        eventRecyclerView.layoutManager = LinearLayoutManager(activity)




        //for(event in calendarViewModel.events.value!!) {

            /*
            val date = event.startDate
            if (allCalendarEvents[date] == null) {
                val newList: ArrayList<Event> = arrayListOf()
                newList.add(event)
                allCalendarEvents[date] = newList
            } else {
                allCalendarEvents[date]?.add(event)
            }*/
        //}



        /*
        val data = ArrayList<CalendarEvent>()

        // This will pass the ArrayList to our Adapter
        val adapter = CalendarAdapter(data)

        // Setting the Adapter with the recyclerview
        eventRecyclerView.adapter = adapter*/

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView) as CalendarView
        dateTV = view.findViewById(R.id.dateView) as TextView

        calendarViewModel.fetchEvents()


        //set current date as default
        var selectedDate: String
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        selectedDate = current.format(formatter)
        dateTV.setText(selectedDate)

        // changing dates (and lists) by clicking dates on calendar
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            selectedDate = (year.toString() + "-" + (month + 1) + "-" + dayOfMonth)
            dateTV.setText(selectedDate)

            // load list by date selected
            var currentList = allCalendarEvents[selectedDate]
            if (currentList == null) {
                // no list, load an empty list
                val emptyList: ArrayList<Event> = arrayListOf()
                currentList = emptyList
            }

            val adapter = EventAdapter(currentList)
            eventRecyclerView.adapter = adapter
        }

        // add events to list
        addBtn = view.findViewById(R.id.btnEventAdd)
        addBtn.setOnClickListener {
            for (event in calendarViewModel.events.value!!) {
                val date = event.startDate
                if (allCalendarEvents[date] == null) {
                    val newList: ArrayList<Event> = arrayListOf()
                    newList.add(event)
                    allCalendarEvents[date] = newList
                } else {
                    allCalendarEvents[date]?.add(event)
                }
            }


            /*
            TO DO: List is not loading, get query seems to be ok, but retrieved list is null
                - try use debugger
                - list seems to be loaded when using button listener (ie delay between fetch and loop)
                -
             */

            /*
            val editText = view.findViewById<EditText>(R.id.et_eventInput)

            val value = editText.text.toString()
            calendarViewModel.createEvent(value)
            if (value.isNotEmpty()) {
                var currentList: ArrayList<Event> = arrayListOf()
                if (allCalendarEvents[selectedDate] == null) {
                    currentList.add(Event(value))
                    allCalendarEvents[selectedDate] = currentList
                } else {
                    allCalendarEvents[selectedDate]?.add(Event(value))
                    currentList = allCalendarEvents[selectedDate]!!
                }
                val adapter = EventAdapter(currentList)
                eventRecyclerView.adapter = adapter
                editText.text = null
            }*/
        }
    }


}