package com.example.explorexpert.ui.view

import HistoricalWeatherFragment
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RelativeLayout
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
import com.example.explorexpert.adapters.ForecastAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.util.Date
import java.util.Locale


const val TAG = "WeatherFragment"

class WeatherFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var textView: TextView
    private lateinit var status: TextView
    private lateinit var autoCompleteAdapter: ArrayAdapter<AutocompletePrediction>
    private lateinit var appInfo: ApplicationInfo
    private lateinit var addressTextView: TextView

    // Default location (University of Waterloo)
    private var defLatitude = 43.4723
    private var latitude  = 43.4723
    private var defLongitude = -80.5449
    private var longitude = -80.5449
    private lateinit var weatherIconImageView: ImageView

    // Forecast properties
    private lateinit var forecastRecyclerView: RecyclerView
    private lateinit var forecastAdapter: ForecastAdapter
    private val forecastItems: ArrayList<ForecastAdapter.ForecastItem> = ArrayList()

    private var mapLatLng: LatLng? = null

    private lateinit var startDateInput: Button
    private lateinit var startDateText: EditText
    private lateinit var endDateInput: Button
    private lateinit var endDateText: EditText
    private lateinit var fetchButton: Button


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
        weatherIconImageView = view.findViewById(R.id.weatherIcon)


        startDateText = view.findViewById<EditText>(R.id.startDateText)
        startDateInput = view.findViewById<Button>(R.id.select1)
        endDateText = view.findViewById<EditText>(R.id.endDateText)
        endDateInput = view.findViewById<Button>(R.id.select2)
        fetchButton = view.findViewById<Button>(R.id.fetchHistoricalWeatherButton)

        startDateInput.setOnClickListener { view: View? ->
            showDatePickerDialog(
                startDateText
            )
        }
        endDateInput.setOnClickListener { view: View? ->
            showDatePickerDialog(
                endDateText
            )
        }

        val startAutocomplete =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    if (intent != null) {
                        val place = Autocomplete.getPlaceFromIntent(intent)
                        latitude = place.latLng?.latitude ?: defLatitude
                        longitude = place.latLng?.longitude ?: defLongitude
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

        fetchButton.setOnClickListener {
            val startDate = startDateText.text.toString()
            val endDate = endDateText.text.toString()
            val address = addressTextView.text.toString()

            if (startDate == "" || endDate == "" || address == "") {
                Snackbar.make(
                    this.requireView(),
                    "Please select dates!",
                    Snackbar.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val historicalWeatherFragment = HistoricalWeatherFragment()
            val bundle = Bundle()
            bundle.putDouble("latitude", latitude)
            bundle.putDouble("longitude", longitude)
            bundle.putString("startDate", startDate)
            bundle.putString("endDate", endDate)
            bundle.putString("address", address)
            historicalWeatherFragment.arguments = bundle

            // Navigate to HistoricalWeatherFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, historicalWeatherFragment)
                .addToBackStack(null)
                .commit()
        }


        // Initialize the RecyclerView for forecast
        forecastRecyclerView = view.findViewById(R.id.forecastRecyclerView)
        forecastAdapter = ForecastAdapter(forecastItems, requireContext())
        forecastRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        forecastRecyclerView.adapter = forecastAdapter




        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), appId)
        }
        val placesClient = Places.createClient(requireContext())

        // Setup search button click listener
        val searchButton: Button = view.findViewById(R.id.searchIcon)
        searchButton.setOnClickListener {
            val placeFields = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, placeFields)
                .build(requireContext())
            startAutocomplete.launch(intent)
        }
        return view
    }

    private fun searchPlace() {

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (mapLatLng == null) {
            obtainLocation()
        } else {
            val latlng = mapLatLng as LatLng

            // Clear mapLatLng to obtain current location on next swap
            mapLatLng = null
            getTemp(latlng.latitude, latlng.longitude)
        }
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
                    getTemp(defLatitude, defLongitude)
                }
            }
    }

    fun setMapLocation(latlng: LatLng) {
        mapLatLng = latlng
    }

    private fun getTemp(latitude: Double, longitude: Double) {
        val appId = appInfo.metaData?.getString("openweather.API_KEY")
        val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?" +
                "lat=$latitude&lon=$longitude&units=metric&appid=$appId"
        Log.i(
            TAG,
            "API key: $appId"        )

        val forecastUrl = "https://api.openweathermap.org/data/2.5/forecast?" +
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
                    val city = obj.getString("name")
                    val country = sys.getString("country")

                    val address = "$city, $country"
                    addressTextView.text = address
                    textView.text = formattedTemp
                    val weatherStatus = weather.getString("main")
                    status.text = weatherStatus

                    Log.i(TAG, "Weather status received: '$weatherStatus'")
                    loadWeatherIcon(weatherIconImageView, weatherStatus)

                    // Fetch travel advisory based on the country code
                    fetchTravelAdvisoryData(country)

                } catch (e: JSONException) {
                    textView.text = "Error parsing JSON"
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                // Handle different types of errors from the API
                val errorMessage = when (error) {
                    is com.android.volley.TimeoutError -> "Request Timeout. Please check your internet connection."
                    is com.android.volley.NoConnectionError -> "No Internet Connection. Please check your network settings."
                    is com.android.volley.NetworkError -> "Network Error. Please try again later."
                    is com.android.volley.ServerError -> "Server Error. Please try again later."
                    is com.android.volley.ParseError -> "Parse Error. Please try again later."
                    else -> "An error occurred. Please try again later."
                }
                // Set the text size for the error message
                textView.textSize = 25f // Change the text size (16f is an example size)

                textView.text = errorMessage
            })

        val stringReq2 = StringRequest(Request.Method.GET, forecastUrl,
            Response.Listener<String> { response ->
                try {
                    val obj = JSONObject(response)
                    val forecastList = obj.getJSONArray("list")

                    val newForecastItems: ArrayList<ForecastAdapter.ForecastItem> = ArrayList()

                    // Track unique dates to avoid duplicates
                    val uniqueDates = HashSet<String>()

                    for (i in 1 until forecastList.length()) {
                        val forecastObj = forecastList.getJSONObject(i)
                        val main = forecastObj.getJSONObject("main")
                        val weather = forecastObj.getJSONArray("weather").getJSONObject(0)
                        val temp = main.getDouble("temp")
                        val formattedTemp = String.format("%.1f°C", temp)
                        val weatherStatus = weather.getString("main")
                        val dateTxt = forecastObj.getString("dt_txt")
                        val date = getDate(dateTxt)
                        val weatherIcon = weather.getString("icon")

                        // Check if the date is already added, if not add the item
                        if (date !in uniqueDates) {
                            val forecastItem =
                                ForecastAdapter.ForecastItem(date, formattedTemp, weatherStatus, weatherIcon)
                            forecastItems.add(forecastItem)
                            newForecastItems.add(forecastItem)
                            uniqueDates.add(date)
                        }
                    }

                    // Update forecastItems with the new forecast data
                    forecastItems.clear()
                    forecastItems.addAll(newForecastItems)
                    forecastAdapter.notifyDataSetChanged()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                textView.text = "Error: ${error.message}"
            })



        queue.add(stringReq)
        queue.add(stringReq2)
    }

    // Helper function to convert UTC timestamp to date format
    private fun getDate(dateTxt: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(dateTxt) ?: Date()
        val outputDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        return outputDateFormat.format(calendar.time)
    }


    // Modify loadWeatherIcon method to handle different weather statuses
    private fun loadWeatherIcon(imageView: ImageView, weatherStatus: String) {
        Log.i(TAG, "Weather status received: '$weatherStatus'")
        val imageName: String = when (weatherStatus) {
            "Thunderstorm" -> "thunderstorm.png"
            "Clouds" -> "cloud.png"
            "Rain" -> "rainy.png"
            "Snow" -> "snow.png"
            // Add more cases as needed for other weather statuses
            else -> "clear_sky.png"
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


    // Function to show the DatePickerDialog
    private fun showDatePickerDialog(dateInput: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        val day = calendar[Calendar.DAY_OF_MONTH]

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDay = String.format("%02d", selectedDay)
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$formattedDay"
                dateInput.setText(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }




}