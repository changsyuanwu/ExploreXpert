package com.example.explorexpert.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.data.model.Event
import com.example.explorexpert.databinding.EventItemBinding
import com.example.explorexpert.ui.viewmodel.CalendarViewModel

import javax.inject.Inject

class EventAdapter(
    private val itemClickListener: ItemClickListener
) : ListAdapter<Event, EventAdapter.ViewHolder>(DiffCallback()){

    @Inject
    lateinit var calendarViewModel: CalendarViewModel
    inner class ViewHolder(private val binding: EventItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.tvEvent.text = event.name

            if (event.startDate == event.endDate) {
                binding.tvEventDate.text = event.startDate
            } else {
                val dateRange = event.startDate + " - " + event.endDate
                binding.tvEventDate.text = dateRange
            }

            binding.btnDelEvent.setOnClickListener {
                itemClickListener.onItemClick(event)
            }
        }


    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = EventItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = getItem(position)

        holder.bind(event)
    }

    interface ItemClickListener {
        fun onItemClick(event: Event)
    }

    private class DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }

}