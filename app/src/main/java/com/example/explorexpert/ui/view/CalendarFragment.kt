package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.R
import com.example.explorexpert.adapters.CalendarAdapter
import com.example.explorexpert.data.model.CalendarEvent
import com.example.explorexpert.databinding.FragmentCalendarBinding
import com.example.explorexpert.ui.viewmodel.CalendarViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CalendarFragment : Fragment() {

    companion object {
        private val TAG = "CalendarFragment"
    }

    @Inject
    lateinit var calendarViewModel: CalendarViewModel
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!


    lateinit var dateTV: TextView
    lateinit var calendarView: CalendarView
    lateinit var addBtn: Button
    lateinit var eventRecyclerView: RecyclerView
    val allCalendarEvents = mutableMapOf<String, ArrayList<CalendarEvent>>()
    var allEvents = ArrayList<CalendarEvent>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                val emptyList: ArrayList<CalendarEvent> = arrayListOf()
                currentList = emptyList
            }
            val recyclerAdapter = context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_list_item_1,
                    currentList!!
                )
            }

            val adapter = CalendarAdapter(currentList)
            eventRecyclerView.adapter = adapter
        }

        addBtn = view.findViewById(R.id.btnEventAdd)
        addBtn.setOnClickListener {
            // add events to list
            addBtn = view.findViewById(R.id.btnEventAdd)
            val editText = view.findViewById<EditText>(R.id.et_eventInput)
            addBtn.setOnClickListener {
                val value = editText.text.toString()

                if (value.isEmpty()) {
                    //Toast.makeText(context, "Please fill out the blank", Toast.LENGTH_LONG).show()
                } else {
                    var currentList: ArrayList<CalendarEvent> = arrayListOf()
                    if (allCalendarEvents[selectedDate] == null) {
                        currentList.add(CalendarEvent(value))
                        allCalendarEvents[selectedDate] = currentList
                    } else {
                        allCalendarEvents[selectedDate]?.add(CalendarEvent(value))
                        currentList = allCalendarEvents[selectedDate]!!
                    }
                    val adapter = CalendarAdapter(currentList)
                    eventRecyclerView.adapter = adapter
                    editText.setText(null)
                }
            }
        }
    }
}