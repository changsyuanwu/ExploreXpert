package com.example.explorexpert.adapters

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.R
import com.example.explorexpert.data.model.SavedItemType
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.databinding.SimpleTripItemBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SimpleTripAdapter(
    private val tripRepo: TripRepository,
    private val itemClickListener: ItemClickListener,
): ListAdapter<Trip, SimpleTripAdapter.ViewHolder>(DiffCallback()) {

    companion object {
        const val TAG = "SimpleTripAdapter"
    }

    inner class ViewHolder(private val binding: SimpleTripItemBinding): RecyclerView.ViewHolder(binding.root) {

        val context: Context = binding.imgTrip.context
        private val placesClient = Places.createClient(context)

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

            CoroutineScope(Dispatchers.Main).launch {
                val savedItems = tripRepo.getSavedItemsFromTrip(trip)

                var placePhotoBitmap: Bitmap? = null

                // Get the last place that has an usable image
                // We want the last one since items are sorted by newest first (and we want the oldest)
                val placeToUseImageFor = savedItems.findLast { item ->
                    if (item.type == SavedItemType.PLACE) {
                        val place = getPlaceById(item.placeId)

                        if (place != null) {
                            placePhotoBitmap = getPlacePhoto(place)
                            return@findLast placePhotoBitmap != null
                        }
                    }
                    return@findLast false
                }

                // If we managed to find a place with an usable image
                if (placeToUseImageFor != null) {
                    binding.imgTrip.setImageBitmap(placePhotoBitmap)
                }
                else {
                    binding.imgTrip.setImageResource(R.drawable.trip_default_image)
                }
            }
        }

        private suspend fun getPlacePhoto(place: Place): Bitmap? {
            val photoMetadata = place.photoMetadatas
            if (photoMetadata == null || photoMetadata.isEmpty()) {
                Log.w(SavedItemAdapter.TAG, "No photo metadata for place: ${place.name}")
                return null
            }
            val firstPhotoMetadata = photoMetadata.first()

            val photoRequest = FetchPhotoRequest.builder(firstPhotoMetadata)
                .build()

            try {
                val response = placesClient.fetchPhoto(photoRequest).await()
                return response.bitmap
            }
            catch (e: Exception) {
                if (e is ApiException) {
                    Log.e(SavedItemAdapter.TAG, "Place photo not found: ${e.message}")
                }
            }
            return null
        }

        private suspend fun getPlaceById(placeId: String): Place? {
            // Which fields to get from the place data
            val placeFields = listOf(Place.Field.NAME, Place.Field.PHOTO_METADATAS)

            val request = FetchPlaceRequest.newInstance(placeId, placeFields)

            try {
                val response = placesClient.fetchPlace(request).await()
                return response.place
            }
            catch (e: Exception) {
                if (e is ApiException) {
                    Log.e(SavedItemAdapter.TAG, "Place not found: ${e.message}")
                }
            }
            return null
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