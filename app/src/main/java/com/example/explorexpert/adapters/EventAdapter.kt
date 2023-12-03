package com.example.explorexpert.adapters

import com.example.explorexpert.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.data.model.Event


class EventAdapter(
    private val eventItems: List<Event>,
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventTextView: TextView = itemView.findViewById(R.id.tvEvent)
        val eventDateTextView: TextView = itemView.findViewById(R.id.tvEventDate)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = eventItems[position]
        holder.eventTextView.text = item.name

        if (item.startDate == item.endDate) {
            holder.eventDateTextView.text = item.startDate
        } else {
            val dateRange = item.startDate + " - " + item.endDate
            holder.eventDateTextView.text = dateRange
        }

    }

    override fun getItemCount(): Int {
        return eventItems.size
    }

}