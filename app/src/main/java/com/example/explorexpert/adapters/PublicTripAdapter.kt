package com.example.explorexpert.adapters

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.R
import com.example.explorexpert.data.model.SavedItemType
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.data.repository.UserRepository
import com.example.explorexpert.databinding.PublicTripItemBinding
import com.example.explorexpert.databinding.TripItemBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PublicTripAdapter(
    private val itemClickListener: ItemClickListener,
    private val childFragmentManager: FragmentManager,
    private val userRepo: UserRepository,
    private val tripRepo: TripRepository,
) : ListAdapter<Trip, PublicTripAdapter.ViewHolder>(DiffCallback()) {

    companion object {
        const val TAG = "PublicTripAdapter"
    }

    private lateinit var appInfo: ApplicationInfo

    inner class ViewHolder(
        private val binding: PublicTripItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val context: Context = binding.cardImage.context
        private lateinit var placesClient: PlacesClient

        fun bind(publicTrip: Trip) {
            configurePlacesSDK()

            binding.txtTripName.text = publicTrip.name

            if (publicTrip.savedItemIds.size == 1) {
                binding.btnSavedItems.text = "1 item"
            }
            else {
                binding.btnSavedItems.text = "${publicTrip.savedItemIds.size} items"
            }

            CoroutineScope(Dispatchers.Main).launch {
                val tripOwner = userRepo.getUserById(publicTrip.ownerUserId)
                if (tripOwner != null) {
                    binding.txtTripOwner.text = "By ${tripOwner.firstName} ${tripOwner.lastName}"
                }

                configureTripPhoto(publicTrip)
            }
        }

        private suspend fun configureTripPhoto(publicTrip: Trip) {
            val savedItems = tripRepo.getSavedItemsFromTrip(publicTrip)

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
            } catch (e: Exception) {
                if (e is ApiException) {
                    Log.e(SavedItemAdapter.TAG, "Place photo not found: ${e.message}")
                }
            }
            return null
        }

        private suspend fun getPlaceById(placeId: String): Place? {
            // Which fields to get from the place data
            val placeFields = listOf(
                Place.Field.NAME,
                Place.Field.PHOTO_METADATAS
            )

            val request = FetchPlaceRequest.newInstance(placeId, placeFields)

            try {
                val response = placesClient.fetchPlace(request).await()
                return response.place
            } catch (e: Exception) {
                if (e is ApiException) {
                    Log.e(SavedItemAdapter.TAG, "Place not found: ${e.message}")
                }
            }
            return null
        }

        private fun configurePlacesSDK() {
            configureAppInfo()

            val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

            Places.initialize(context, appId)

            placesClient = Places.createClient(context)
        }

        private fun configureAppInfo() {
            appInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PublicTripAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PublicTripItemBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PublicTripAdapter.ViewHolder, position: Int) {
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