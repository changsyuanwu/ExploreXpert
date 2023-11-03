package com.example.explorexpert.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.TripItemBinding
import com.example.explorexpert.utils.ImageLoaderUtil

class TripAdapter(
    private val itemClickListener: ItemClickListener
): ListAdapter<Trip, TripAdapter.ViewHolder>(DiffCallback()) {

    companion object {
        const val TAG = "TripAdapter"
    }

    inner class ViewHolder(private val binding: TripItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: Trip) {
            binding.txtTripName.text = trip.name
            binding.btnSavedItems.text = "${trip.savedItemIds.size} items"

            // ImageLoaderUtil.loadImageIntoView(binding.imgTrip, URL, false)

            binding.tripItemContainer.setOnClickListener {
                itemClickListener.onItemClick(trip)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TripItemBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = getItem(position)
        holder.bind(trip)
    }

    interface ItemClickListener {
        fun onItemClick(trip: Trip)
    }

    private class DiffCallback : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem == newItem
        }
    }
}