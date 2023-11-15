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
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private lateinit var appInfo: ApplicationInfo
    private var weatherItems: MutableList<WeatherAdapter.WeatherItem> = mutableListOf()
    // Default location (University of Waterloo)
    private var latitude = 43.4723
    private var longitude = -80.5449
    private lateinit var searchText : AutoCompleteTextView



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
        weatherRecyclerView = view.findViewById(R.id.weatherRecyclerView)
        weatherAdapter = WeatherAdapter(weatherItems)
        weatherRecyclerView.layoutManager = LinearLayoutManager(context)
        weatherRecyclerView.adapter = weatherAdapter
        val weatherIconImageView: ImageView = view.findViewById(R.id.weatherIcon)
        loadWeatherIcon(weatherIconImageView, "snow.png")

        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), appId);
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
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields).build(requireContext())
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
                    latitude = location.latitude
                    longitude = location.longitude
                    getTemp(latitude, longitude)
                } else {
                    println("Location is null")
                    getTemp(latitude, longitude)
                }
            }
    }

    private fun getTemp(latitude: Double, longitutde: Double) {
        val appId = appInfo.metaData?.getString("openweather.API_KEY")
        val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?" +
                "lat=$latitude&lon=$longitude&units=metric&appid=$appId"
        val queue = Volley.newRequestQueue(requireContext())

        val stringReq = StringRequest(Request.Method.GET, weatherUrl,
            Response.Listener<String> { response ->
                try {
                    val obj = JSONObject(response)
                    val main = obj.getJSONObject("main")
                    val sys = obj.getJSONObject("sys")
                    val weather = obj.getJSONArray("weather").getJSONObject(0)

                    val temp = main.getDouble("temp")
                    val formattedTemp = String.format("%.1f°C", temp)

                    val country = sys.getString("country")
                    textView.text = formattedTemp
                    status.text = weather.getString("main")

                    // Add weather data to the list for RecyclerView
                    val weatherItem =
                        WeatherAdapter.WeatherItem(formattedTemp, weather.getString("main"))
                    weatherItems.add(weatherItem)
                    weatherAdapter.notifyDataSetChanged()

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
                    Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.address}")
                    // Now you can use 'place' to get details of the selected place
                    getTemp(
                        place.latLng?.latitude ?: latitude,
                        place.latLng?.longitude ?: longitude
                    )
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
        val imageName: String = when (weatherStatus.toLowerCase()) {
            "clear" -> "clear_sky.png"
            "clouds" -> "cloudy.png"
            "rain" -> "rainy.png"
            "snow" -> "snow.png"
            // Add more cases as needed for other weather statuses
            else -> "unknown_weather.png"
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


}
