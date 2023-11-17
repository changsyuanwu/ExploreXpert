package com.example.explorexpert.ui.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.explorexpert.R
import com.example.explorexpert.data.model.WeatherAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream


private const val TAG = "WeatherFragment"

class WeatherFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var textView: TextView
    private lateinit var status: TextView
    private lateinit var weatherRecyclerView: RecyclerView
    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var autoCompleteAdapter: ArrayAdapter<AutocompletePrediction>
    private lateinit var appInfo: ApplicationInfo
    private var weatherItems: MutableList<WeatherAdapter.WeatherItem> = mutableListOf()
    private lateinit var addressTextView: TextView

    // Default location (University of Waterloo)
    private var defLatitude = 43.4723
    private var defLongitude = -80.5449
    private lateinit var weatherIconImageView: ImageView


    override fun onAttach(context: Context) {
        super.onAttach(context)
        appInfo = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_weather, container, false)
        textView = view.findViewById(R.id.temp)
        status = view.findViewById(R.id.status)
        addressTextView = view.findViewById(R.id.address)
        weatherRecyclerView = view.findViewById(R.id.weatherRecyclerView)
        weatherAdapter = WeatherAdapter(weatherItems)
        weatherRecyclerView.layoutManager = LinearLayoutManager(context)
        weatherRecyclerView.adapter = weatherAdapter
        weatherIconImageView = view.findViewById(R.id.weatherIcon)
        loadWeatherIcon(weatherIconImageView, "Snow")

        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), appId)
        }
        val placesClient = Places.createClient(requireContext())

        // Setup search button click listener
        val searchButton: Button = view.findViewById(R.id.searchIcon)
        searchButton.setOnClickListener {
            searchPlace()
        }
        return view
    }

    private fun searchPlace() {
        val placeFields = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields)
            .build(requireContext())
        startAutocomplete.launch(intent)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        obtainLocation()
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    getTemp(location.latitude, location.longitude)
                } else {
                    println("Location is null")
                    getTemp(defLatitude, defLongitude)
                }
            }
    }

    private fun getTemp(latitude: Double, longitude: Double) {
        val appId = appInfo.metaData?.getString("openweather.API_KEY")
        val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?" +
                "lat=$latitude&lon=$longitude&units=metric&appid=$appId"
        val queue = Volley.newRequestQueue(requireContext())

        val stringReq = StringRequest(Request.Method.GET, weatherUrl,
            Response.Listener<String> { response ->
                try {
                    val obj = JSONObject(response)
                    Log.i(TAG, "Object: ${obj}")
                    val main = obj.getJSONObject("main")
                    val sys = obj.getJSONObject("sys")
                    val weather = obj.getJSONArray("weather").getJSONObject(0)

                    val temp = main.getDouble("temp")
                    val formattedTemp = String.format("%.1f°C", temp)
                    val city = obj.getString("name")
                    val country = sys.getString("country")

                    val address = "$city, $country"
                    addressTextView.text = address
                    textView.text = formattedTemp
                    val weatherStatus = weather.getString("main")
                    status.text = weatherStatus

                    loadWeatherIcon(weatherIconImageView, weatherStatus)

                    // Add weather data to the list for RecyclerView
                    val weatherItem =
                        WeatherAdapter.WeatherItem(formattedTemp, weather.getString("main"))
                    weatherItems.add(weatherItem)
                    weatherAdapter.notifyDataSetChanged()
                    // Fetch travel advisory based on the country code
                    fetchTravelAdvisoryData(country)

                } catch (e: JSONException) {
                    textView.text = "Error parsing JSON"
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                textView.text = "Error: ${error.message}"
            })

        queue.add(stringReq)
    }

    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    Log.i(
                        TAG,
                        "Place: ${place.latLng?.latitude}, ${place.latLng?.longitude}, ${place.address}"
                    )
                    val latitude = place.latLng?.latitude ?: defLatitude
                    val longitude = place.latLng?.longitude ?: defLongitude
                    getTemp(latitude, longitude)
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                Log.i(TAG, "User canceled autocomplete")
            } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
                // Handle Autocomplete error
                val status = Autocomplete.getStatusFromIntent(result.data)
                Log.e(TAG, "Error during autocomplete: ${status.statusMessage}")
            }
        }

    // Modify loadWeatherIcon method to handle different weather statuses
    private fun loadWeatherIcon(imageView: ImageView, weatherStatus: String) {
        Log.d(TAG, "Weather status received: '$weatherStatus'")
        val imageName: String = when (weatherStatus) {
            "Clear" -> "clear_sky.png"
            "Clouds" -> "cloudy.png"
            "Rain" -> "rainy.png"
            "Snow" -> "snow.png"
            // Add more cases as needed for other weather statuses
            else -> "snow.png"
        }

        try {
            // Load the image from the assets folder
            val inputStream: InputStream = requireContext().assets.open(imageName)
            val drawable = Drawable.createFromStream(inputStream, null)
            imageView.setImageDrawable(drawable)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    /////////////////////////////////////////////////////////////////////////////
    // Travel Advisory section

    ///////////////////////////////////////////////////////////////////////////

    private fun fetchTravelAdvisoryData(countryCode: String) {
        val url = "https://www.travel-advisory.info/api?countrycode=$countryCode"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                handleTravelAdvisoryResponse(response, countryCode)
            },
            { error ->
                // Handle error
                Log.e(TAG, "Volley Error: ${error.message}")
            })

        // Add the request to the RequestQueue
        Volley.newRequestQueue(requireContext()).add(stringRequest)
    }

    private fun handleTravelAdvisoryResponse(responseData: String?, countryCode: String) {
        try {
            val obj = JSONObject(responseData)
            val data = obj.getJSONObject("data")
            val advisory = data.getJSONObject(countryCode).getJSONObject("advisory")
            val score = advisory.getDouble("score")
            Log.i(TAG, "Object: ${score}")
            val ratingBar: RatingBar? = view?.findViewById(R.id.ratingBar)
            val riskMessageTextView: TextView? = view?.findViewById(R.id.riskMessageTextView)
            val travelWarningSection: RelativeLayout? =
                view?.findViewById(R.id.travelWarningSection)

            // Set RatingBar rating
            ratingBar?.rating = score.toFloat()

            // Set the message based on score and update UI colors accordingly
            val message = getRiskMessage(score)
            riskMessageTextView?.text = message

            // Update UI based on risk level (for background color)
            updateUIBasedOnRiskLevel(score, travelWarningSection)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Function to get the risk message based on the score
    private fun getRiskMessage(score: Double): String {
        return when {
            score >= 4.5 -> "Extreme Warning: You should avoid any trips. Potential harm to your health and well-being."
            score >= 3.5 -> "High Risk: Travel should be reduced to a necessary minimum and be conducted with good preparation and high attention."
            score >= 2.5 -> "Medium Risk: High attention is advised when traveling around."
            else -> "Low Risk: Travel is relatively safe."
        }
    }

    // Function to update UI colors based on the risk level
    private fun updateUIBasedOnRiskLevel(score: Double, travelWarningSection: RelativeLayout?) {
        val color = when {
            score >= 4.5 -> Color.parseColor("#FFCACA") // Pastel Red
            score >= 3.5 -> Color.parseColor("#FFF7AE") // Pastel Yellow
            score >= 2.5 -> Color.parseColor("#C9E6FF") // Pastel Blue
            else -> Color.parseColor("#C4FFC4") // Pastel Green
        }

        // Update RelativeLayout background color
        travelWarningSection?.setBackgroundColor(color)
    }


}
