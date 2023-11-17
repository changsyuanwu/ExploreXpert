package com.example.explorexpert.adapters

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.text.capitalize
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.R
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.SavedItemType
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
import java.util.Locale

class SavedItemAdapter(
    private val itemClickListener: ItemClickListener,
    private val isInTripDialog: Boolean = true,
): ListAdapter<SavedItem, SavedItemAdapter.ViewHolder>(DiffCallback()){

    companion object {
        const val TAG = "SavedItemAdapter"
    }

    private lateinit var appInfo: ApplicationInfo

    inner class ViewHolder(private val binding: SavedItemBinding): RecyclerView.ViewHolder(binding.root) {

        val context: Context = binding.imgItem.context
        private lateinit var placesClient: PlacesClient

        fun bind(savedItem: SavedItem) {
            configurePlacesSDK()

            binding.txtItemType.text = savedItem.type.toString()

            binding.txtItemName.text = savedItem.title

            binding.txtItemDescription.text = savedItem.description

            binding.savedItemContainer.setOnClickListener {
                itemClickListener.onItemClick(savedItem)
            }

            when (savedItem.type) {
                SavedItemType.NOTE -> {
                    binding.imgItem.setImageResource(R.drawable.trip_default_image)
                }

                SavedItemType.LINK -> {
                    // load image from website
                }

                SavedItemType.PLACE -> {
                    // load image from google places
                    CoroutineScope(Dispatchers.Main).launch {
                        val place = getPlaceById(savedItem.placeId)

                        if (place != null) {
                            val placePhotoBitmap = getPlacePhoto(place)
                            if (placePhotoBitmap != null) {
                                binding.imgItem.setImageBitmap(placePhotoBitmap)
                            }
                        }
                    }

                }

                SavedItemType.BLANK -> {
                    // Do nothing
                }
            }
        }

        private suspend fun getPlacePhoto(place: Place): Bitmap? {
            val photoMetadata = place.photoMetadatas
            if (photoMetadata == null || photoMetadata.isEmpty()) {
                Log.w(TAG, "No photo metadata for place: ${place.name}")
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
                    Log.e(TAG, "Place photo not found: ${e.message}")
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
                    Log.e(TAG, "Place not found: ${e.message}")
                }
            }
            return null
        }

        private fun configurePlacesSDK() {
            configureAppInfo()

            val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

            Places.initialize(context, appId);

            placesClient = Places.createClient(context)
        }

        private fun configureAppInfo() {
            appInfo = context.packageManager
                .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = SavedItemBinding.inflate(inflater, parent, false)

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