package com.example.google.playservices.placecomplete.com.example.explorexpert.ui.viewmodel

import android.content.Context
import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class PlaceAutocompleteAdapter(
    context: Context,
    private val geoDataClient: GeoDataClient,
    private var bounds: LatLngBounds?,
    private val placeFilter: AutocompleteFilter
) : ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_expandable_list_item_2, android.R.id.text1), Filterable {

    companion object {
        private const val TAG = "PlaceAutocompleteAdapter"
        private val STYLE_BOLD: CharacterStyle = StyleSpan(Typeface.BOLD)
    }

    private var resultList: ArrayList<AutocompletePrediction> = ArrayList()

    fun setBounds(bounds: LatLngBounds) {
        this.bounds = bounds
    }

    override fun getCount(): Int = resultList.size

    override fun getItem(position: Int): AutocompletePrediction? = resultList[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)

        val item = getItem(position)

        val textView1: TextView = row.findViewById(android.R.id.text1)
        val textView2: TextView = row.findViewById(android.R.id.text2)
        textView1.text = item?.getPrimaryText(STYLE_BOLD)
        textView2.text = item?.getSecondaryText(STYLE_BOLD)

        return row
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()

            // We need a separate list to store the results since this is run asynchronously.
            var filterData = ArrayList<AutocompletePrediction>()

            // Skip the autocomplete query if no constraints are given.
            if (!constraint.isNullOrBlank()) {
                // Query the autocomplete API for the (constraint) search string.
                filterData = getAutocomplete(constraint.toString()) ?: ArrayList()
            }

            results.values = filterData
            results.count = filterData.size

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            if (results.count > 0) {
                // The API returned at least one result, update the data.
                resultList = results.values as ArrayList<AutocompletePrediction>
                notifyDataSetChanged()
            } else {
                // The API did not return any results, invalidate the data set.
                notifyDataSetInvalidated()
            }
        }

        override fun convertResultToString(resultValue: Any?): CharSequence {
            // Override this method to display a readable result in the AutocompleteTextView when clicked.
            return if (resultValue is AutocompletePrediction) {
                resultValue.getFullText(null)
            } else {
                super.convertResultToString(resultValue)
            }
        }
    }

    private fun getAutocomplete(constraint: String): ArrayList<AutocompletePrediction>? {
        runBlocking(Dispatchers.IO) {
            try {
                val results: Task<AutocompletePredictionBufferResponse> =
                    geoDataClient.getAutocompletePredictions(constraint, bounds, placeFilter)

                Tasks.await(results, 60, TimeUnit.SECONDS)

                val autocompletePredictions = results.getResult()

                // Freeze the results immutable representation that can be stored safely.
                resultList = DataBufferUtils.freezeAndClose(autocompletePredictions)
            } catch (e: ExecutionException) {
                // If the query did not complete successfully return null
                Toast.makeText(
                    context,
                    "Error contacting API: ${e.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: TimeoutException) {
                e.printStackTrace()
            }
        }
        return resultList
    }
}
