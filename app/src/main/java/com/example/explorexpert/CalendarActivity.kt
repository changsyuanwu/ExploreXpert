package com.example.explorexpert

import android.os.Bundle
import android.widget.CalendarView
import androidx.activity.ComponentActivity


class CalendarActivity : ComponentActivity() {

    lateinit var calendarView: CalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.calendar_activity)
        calendarView = findViewById(R.id.calendarView)

    }
}
