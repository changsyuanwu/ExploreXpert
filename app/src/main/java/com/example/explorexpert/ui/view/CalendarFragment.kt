package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.explorexpert.R
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CalendarFragment : Fragment() {

    lateinit var dateTV: TextView
    lateinit var calendarView: CalendarView
    lateinit var addButton: FloatingActionButton
    lateinit var eventListView : ListView
    val allCalendarEvents = mutableMapOf<String, ArrayList<String>>()


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

        var context_temp = context
        //var listAdapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1)

        calendarView.setOnDateChangeListener{
            view, year, month, dayOfMonth -> val Date = (dayOfMonth.toString() + "-" + (month + 1) + "-" + year)
            dateTV.setText(Date)



            // load list by date selected
            //val adapter = MapAdapter(this, android.R.layout.simple_list_item_1, allCalendarEvents)
            if (allCalendarEvents[Date] != null) {
                var myList: ArrayList<String> = arrayListOf()
                myList = allCalendarEvents[Date]!!
                var listAdapter = context_temp?.let { ArrayAdapter<String>(it, android.R.layout.simple_list_item_1, myList) }
                eventListView.setAdapter(listAdapter)
            }

        }


        addButton = view.findViewById(R.id.addEventButton) as FloatingActionButton
        addButton.setOnClickListener{
            val showPopup = PopUpFragment()
            showPopup.show((activity as AppCompatActivity).supportFragmentManager, "showPopUp")
        }
    }

}