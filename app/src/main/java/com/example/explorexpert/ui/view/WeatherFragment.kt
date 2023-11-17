package com.example.explorexpert.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
//import androidx.compose.ui.tooling.data.EmptyGroup.location
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.explorexpert.MainActivity
import com.example.explorexpert.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import org.json.JSONException
import org.json.JSONObject

class WeatherFragment : Fragment() {

    private lateinit var textView: TextView
    private lateinit var status : TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var appInfo: ApplicationInfo

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Stub for grabbing info from map fragment, can move to other parts of module
        if (isAdded) {
            println((requireActivity() as MainActivity).getMapFragment().getMarkedAddress())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        obtainLocation()
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocation() {
        // Latitude and longitude of the University of Waterloo
        val universityLatitude = 43.4723
        val universityLongitude = -80.5449
//        fusedLocationClient.lastLocation
//            .addOnSuccessListener { location: Location? ->
        val appId = appInfo.metaData?.getString("WEATHER_API_KEY")
        //get latitude and longitude then create http url
        val weatherUrl = "https://api.openweathermap.org/data/2.5/weather?" +
                "lat=$universityLatitude&lon=$universityLongitude&units=metric&appid=db11e9a86227181cf18b0066ec9447cd"
        getTemp(weatherUrl)
//            }
    }


    fun getTemp(weatherUrl: String) {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(requireContext())

        // Request a string response from the provided URL.
        val stringReq = StringRequest(Request.Method.GET, weatherUrl,
            Response.Listener<String> { response ->
                // Parse the JSON response and update the UI
                try {
                    val obj = JSONObject(response)
                    val main = obj.getJSONObject("main")
                    val sys = obj.getJSONObject("sys")
                    val weather = obj.getJSONArray("weather").getJSONObject(0)

                    // Extract temperature as Double and format it to 1 decimal place
                    val temp = main.getDouble("temp")
                    val formattedTemp = String.format("%.1f°C", temp)

                    val country = sys.getString("country")
                    textView.text = formattedTemp
                    status.text = weather.getString("main")

                } catch (e: JSONException) {
                    textView.text = "Error parsing JSON"
                    e.printStackTrace()
                }
            },
            // Handle errors
            Response.ErrorListener { error ->
                textView.text = "Error: ${error.message}"
            })


        // Add the request to the RequestQueue
        queue.add(stringReq)
    }

}



