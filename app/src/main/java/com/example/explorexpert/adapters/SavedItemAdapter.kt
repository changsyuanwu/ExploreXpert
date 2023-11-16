package com.example.explorexpert.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.databinding.SavedItemInTripBinding

class SavedItemAdapter(
    private val itemClickListener: ItemClickListener
): ListAdapter<SavedItem, SavedItemAdapter.ViewHolder>(DiffCallback()){

    companion object {
        const val TAG = "SavedItemAdapter"
    }

    inner class ViewHolder(private val binding: SavedItemInTripBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(savedItem: SavedItem) {
            binding.txtItemName.text = savedItem.title
            binding.txtItemDescription.text = savedItem.description
            binding.txtItemType.text = savedItem.type.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SavedItemInTripBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val savedItem = getItem(position)
        holder.bind(savedItem)
    }

    interface ItemClickListener {
        fun onItemClick(savedItem: SavedItem)
    }

    private class DiffCallback : DiffUtil.ItemCallback<SavedItem>() {
        override fun areItemsTheSame(oldItem: SavedItem, newItem: SavedItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedItem, newItem: SavedItem): Boolean {
            return oldItem == newItem
        }
    }
}