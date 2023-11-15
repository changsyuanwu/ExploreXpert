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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.explorexpert.R
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CalendarFragment : Fragment() {

    lateinit var dateTV: TextView
    lateinit var calendarView: CalendarView
    lateinit var addBtn: Button
    lateinit var eventListView: ListView
    val allCalendarEvents = mutableMapOf<String, ArrayList<String>>()


    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // build calender
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView) as CalendarView
        dateTV = view.findViewById(R.id.dateView) as TextView
        eventListView = view.findViewById(R.id.eventListView) as ListView


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
                val emptyList: ArrayList<String> = arrayListOf()
                currentList = emptyList
            }
            val listAdapter = context?.let {
                ArrayAdapter(
                    it,
                    android.R.layout.simple_list_item_1,
                    currentList!!
                )
            }
            eventListView.setAdapter(listAdapter)
        }

        // add events to list
        addBtn = view.findViewById(R.id.btnEventAdd)
        val editText = view.findViewById<EditText>(R.id.et_eventInput)
        addBtn.setOnClickListener {
            val value = editText.text.toString()

            if (value.isEmpty()) {
                Toast.makeText(context, "Please fill out the blank", Toast.LENGTH_LONG).show()
            } else {
                var currentList: ArrayList<String> = arrayListOf()
                if (allCalendarEvents[selectedDate] == null) {
                    currentList.add(value)
                    allCalendarEvents[selectedDate] = currentList
                } else {
                    allCalendarEvents[selectedDate]?.add(value)
                    currentList = allCalendarEvents[selectedDate]!!
                }
                val listAdapter = context?.let {
                    ArrayAdapter(
                        it,
                        android.R.layout.simple_list_item_1,
                        currentList
                    )
                }
                eventListView.setAdapter(listAdapter)
                editText.setText(null)
            }
        }

    }


    private fun calendarBuild() {
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val service = Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

}