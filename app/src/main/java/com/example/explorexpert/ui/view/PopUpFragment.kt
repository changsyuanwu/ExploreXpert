package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.explorexpert.R

class PopUpFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar_event_add_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addBtn = view.findViewById<Button>(R.id.btnEventAdd)
        val cancelBtn = view.findViewById<Button>(R.id.btnEventCancel)
        val editText = view.findViewById<EditText>(R.id.et_eventInput)
        val listView = view.findViewById<ListView>(R.id.eventListView)

        cancelBtn.setOnClickListener{
            dismiss()
        }
        addBtn.setOnClickListener{
            val value = editText.text.toString()
            if (value.isEmpty()) {
                Toast.makeText(context,"Please fill out the blank", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context,value,Toast.LENGTH_LONG).show()
                dismiss()
            }
        }

    }

}