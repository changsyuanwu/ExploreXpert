package com.example.explorexpert.adapters

import com.example.explorexpert.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.data.model.CalendarEvent


class CalendarAdapter(
    private val eventItems: List<CalendarEvent>,
) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventTextView: TextView = itemView.findViewById(R.id.tvEvent)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = eventItems[position]
        holder.eventTextView.text = item.name
    }

    override fun getItemCount(): Int {
        return eventItems.size
    }

}