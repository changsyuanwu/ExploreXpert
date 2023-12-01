package com.example.explorexpert.adapters

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.data.model.NearbyPlace
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.databinding.NearbyPlaceItemBinding
import com.example.explorexpert.databinding.SavedItemBinding
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

class NearbyPlaceAdapter(
    private val itemClickListener: ItemClickListener,
    private val tripRepo: TripRepository,
) : ListAdapter<NearbyPlace, NearbyPlaceAdapter.ViewHolder>(DiffCallback()) {
    companion object {
        const val TAG = "NearbyPlaceAdapter"
    }

    private lateinit var appInfo: ApplicationInfo

    inner class ViewHolder(private val binding: NearbyPlaceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val context: Context = binding.cardImage.context
        private lateinit var placesClient: PlacesClient

        fun bind(nearbyPlace: NearbyPlace) {
            configurePlacesSDK()

            binding.txtPlaceName.text = nearbyPlace.name

            binding.txtRating.text = "${nearbyPlace.rating} (${nearbyPlace.numRatings})"

            binding.txtPlaceType.text = nearbyPlace.type
                .split("_")
                .joinToString(" ") {
                    it.replaceFirstChar(Char::titlecase)
                }

            binding.btnFavIcon.setOnClickListener {
                // Let the user add the place to a trip
            }

            binding.itemContainer.setOnClickListener {
                itemClickListener.onItemClick(nearbyPlace)
            }

            // load image from google places
            CoroutineScope(Dispatchers.Main).launch {
                val place = getPlaceById(nearbyPlace.id)

                if (place != null) {
                    val placePhotoBitmap = getPlacePhoto(place)
                    if (placePhotoBitmap != null) {
                        binding.imgPlace.setImageBitmap(placePhotoBitmap)
                    }
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
            } catch (e: Exception) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = NearbyPlaceItemBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nearbyPlace = getItem(position)
        holder.bind(nearbyPlace)
    }

    interface ItemClickListener {
        fun onItemClick(nearbyPlace: NearbyPlace)
    }

    private class DiffCallback : DiffUtil.ItemCallback<NearbyPlace>() {
        override fun areItemsTheSame(oldItem: NearbyPlace, newItem: NearbyPlace): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NearbyPlace, newItem: NearbyPlace): Boolean {
            return oldItem == newItem
        }
    }
}