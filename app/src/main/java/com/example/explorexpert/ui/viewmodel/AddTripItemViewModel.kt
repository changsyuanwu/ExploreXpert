package com.example.explorexpert.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.SavedItem
import com.example.explorexpert.data.model.SavedItemType
import com.example.explorexpert.data.model.Trip
import com.example.explorexpert.data.repository.TripRepository
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import me.angrybyte.goose.Article
import me.angrybyte.goose.ContentExtractor
import me.angrybyte.goose.network.GooseDownloader
import java.net.URL
import javax.inject.Inject


class AddTripItemViewModel@Inject constructor(
    private val auth: FirebaseAuth,
    private val tripRepo: TripRepository,
) : ViewModel() {

    companion object {
        const val TAG = "AddTripItemViewModel"
    }

    private lateinit var trip: Trip

    data class LinkParsingResult(
        val isSuccess: Boolean,
        val message: String,
    )

    private var mutableLinkParsingStatus = MutableLiveData<LinkParsingResult>()
    val linkParsingResult: LiveData<LinkParsingResult> get() = mutableLinkParsingStatus

    fun setTrip(tripToSet: Trip) {
        trip = tripToSet
    }

    fun addNote(title: String, description: String) {
        viewModelScope.launch {
            val item = SavedItem(
                type = SavedItemType.NOTE,
                title = title,
                description = description,
                ownerUserId = auth.currentUser?.uid.toString(),
            )
            tripRepo.addItemToTrip(item, trip)
        }
    }

    fun addPlace(place: Place) {
        viewModelScope.launch {
            val item = SavedItem(
                type = SavedItemType.PLACE,
                title = place.name.toString(),
                placeId = place.id.toString(),
                description = place.address.toString(),
                ownerUserId = auth.currentUser?.uid.toString(),
            )
            tripRepo.addItemToTrip(item, trip)
        }
    }

    fun addLink(linkURL: String, extractor: ContentExtractor) {
        viewModelScope.launch {
            try {
                val extractedArticle = extractor.extractContent(linkURL, false)

                if (extractedArticle == null) {
                    mutableLinkParsingStatus.value = LinkParsingResult(
                        false,
                        "Couldn't load the article, is your URL correct, is your Internet working?"
                    )
                    Log.e(TAG, "Could not load article from $linkURL")
                    return@launch
                }

                var photoURL: String? = null
                if (extractedArticle.topImage != null) { // topImage value holds an absolute URL
                    photoURL = extractedArticle.topImage.imageSrc
                }

                val url = URL(linkURL)
                val urlHost = url.host

                val item = SavedItem(
                    type = SavedItemType.LINK,
                    ownerUserId = auth.currentUser?.uid.toString(),
                    title = extractedArticle.title,
                    photoURL = photoURL,
                    description = urlHost,
                )
                Log.d(TAG, item.toString())

                //tripRepo.addItemToTrip(item, trip)
                mutableLinkParsingStatus.value = LinkParsingResult(
                    true,
                    "Success"
                )
            }
            catch (e: Exception) {
                mutableLinkParsingStatus.value = LinkParsingResult(
                    false,
                    "Failed to add link to trip"
                )
                Log.e(TAG, "Failed to add link to trip: ${e.message}", e)
            }
        }
    }
}