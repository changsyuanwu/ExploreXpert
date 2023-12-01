package com.example.explorexpert.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.databinding.SimpleTripItemBinding

class SimpleTripAdapter(
    private val itemClickListener: ItemClickListener
): ListAdapter<Trip, SimpleTripAdapter.ViewHolder>(DiffCallback()) {

    companion object {
        const val TAG = "SimpleTripAdapter"
    }

    inner class ViewHolder(private val binding: SimpleTripItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(trip: Trip) {
            binding.txtTripName.text = trip.name

            if (trip.savedItemIds.size == 1) {
                binding.txtSavedItems.text = "1 item"
            }
            else {
                binding.txtSavedItems.text = "${trip.savedItemIds.size} items"
            }


            binding.tripItemContainer.setOnClickListener {
                itemClickListener.onItemClick(trip)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SimpleTripItemBinding.inflate(inflater, parent, false)

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