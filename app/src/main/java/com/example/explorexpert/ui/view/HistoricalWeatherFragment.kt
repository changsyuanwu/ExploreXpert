import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.explorexpert.R
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import java.util.Calendar
import android.icu.util.TimeZone
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.example.explorexpert.ui.view.TAG
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.Date

class HistoricalWeatherFragment : Fragment() {
    private lateinit var weatherEntriesLayout: LinearLayout
    private lateinit var appInfo: ApplicationInfo
    private lateinit var addressTextView: TextView
    private var latitude  = 43.4723
    private var longitude = -80.5449

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appInfo = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backArrow: ImageView = view.findViewById(R.id.backArrow)
        backArrow.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historical_weather, container, false)
        weatherEntriesLayout = view.findViewById(R.id.weatherEntriesLayout)
        addressTextView = view.findViewById(R.id.address)

        // Retrieve latitude, longitude, start date, and end date from arguments
        latitude = arguments?.getDouble("latitude", 0.0) ?: 0.0
        longitude = arguments?.getDouble("longitude", 0.0) ?: 0.0
        val startDate = convertDateToUnix(arguments?.getString("startDate", "") ?: "")
        val endDate = convertDateToUnix(arguments?.getString("endDate", "") ?: "")

        val address = arguments?.getString("address")
        addressTextView.text = address

        // Fetch historical weather data
        fetchHistoricalWeatherData(latitude, longitude, startDate, endDate)

        return view
    }

    private fun fetchHistoricalWeatherData(latitude: Double, longitude: Double, startDate: Long, endDate: Long) {
        val apiKey = appInfo.metaData.getString("openweather.API_KEY")
        val historicalWeatherUrl =
            "https://history.openweathermap.org/data/2.5/history/city?lat=$latitude&lon=$longitude&start=$startDate&end=$endDate&units=metric&appid=$apiKey"

        weatherEntriesLayout.removeAllViews()
        val queue: RequestQueue = Volley.newRequestQueue(requireContext())
        // Create a GET request
        val stringRequest = object : StringRequest(
            Method.GET, historicalWeatherUrl,
            Response.Listener { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    Log.i("Hisotrical Weather", "$jsonResponse")
                    displayHistoricalWeatherData(jsonResponse)
                } catch (exc: JSONException) {
                    Log.e("JSON Parsing Error", "Error: ${exc.message}")
                }
            },
            Response.ErrorListener { error ->
                if (error.networkResponse != null) {
                    // If the HTTP error code is 400, handle the error message from the JSON response
                    val errorResponse = String(error.networkResponse.data)
                    try {
                        val errorJson = JSONObject(errorResponse)
                        val errorCode = errorJson.optInt("code")
                        val errorMessage = errorJson.optString("message")
                        if (errorCode == 400000) {
                            displayErrorMessage(errorMessage+": maximum 1 year back")
                        }
                    } catch (exc: JSONException) {
                        Log.e("JSON Parsing Error", "Error: ${exc.message}")
                    }
                }
            }) {
        }

        queue.add(stringRequest)
    }
    private fun displayHistoricalWeatherData(jsonResponse: JSONObject) {
        try {
            val weatherData = jsonResponse.getJSONArray("list")
            // Track unique dates to avoid duplicates
            val uniqueDates = HashSet<String>()

            for (i in 0 until weatherData.length()) {
                val dateWeather = weatherData.getJSONObject(i)

                val timestamp = dateWeather.getLong("dt")
                val temperatureMain = dateWeather.getJSONObject("main")
                val temperatureAvg = temperatureMain.getDouble("temp")
                val weather = dateWeather.getJSONArray("weather")
                val weatherObject = weather.getJSONObject(0)
                val condition = weatherObject.getString("main")

                val formattedDate = convertUnixToDate(timestamp)

                if (!uniqueDates.contains(formattedDate)) {
                    createWeatherEntry(formattedDate, temperatureAvg, condition)
                    // Add the date to the set to avoid duplicates
                    uniqueDates.add(formattedDate)
                }
            }
        } catch (exc: JSONException) {
            Log.e("JSON Parsing Error", "Error: ${exc.message}")
        }
    }
    private fun convertUnixToDate(unixTimestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = Date(unixTimestamp * 1000)
        return dateFormat.format(date)
    }

    private fun createWeatherEntry(date: String, temperatureAvg: Double, condition: String) {
        val entryView = layoutInflater.inflate(R.layout.hist_weather_entry, null)
        val dateTextView: TextView = entryView.findViewById(R.id.dateTextView)
        val temperatureTextView: TextView = entryView.findViewById(R.id.temperatureTextView)
        val conditionTextView: TextView = entryView.findViewById(R.id.weatherConditionTextView)

        dateTextView.text = date
        temperatureTextView.text = "Avg Temp: $temperatureAvg°C"
        conditionTextView.text = condition

        weatherEntriesLayout.addView(entryView)
    }

    private fun convertDateToUnix(dateString: String): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val parsedDate = dateFormat.parse(dateString)
        val unixTimestamp = parsedDate?.time ?: 0
        return unixTimestamp / 1000
    }

    private fun displayErrorMessage(errorMessage: String) {
        val context = requireContext()

        // Create a TextView for the error message
        val errorTextView = TextView(context)
        errorTextView.text = errorMessage

        // Set the warning style for the error message
        errorTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        errorTextView.setBackgroundResource(R.drawable.warning_background)
        errorTextView.setPadding(16, 8, 16, 8)
        errorTextView.gravity = Gravity.CENTER // Center text in the TextView

        // Set the text size
        val textSize = 20f
        errorTextView.textSize = textSize // Set the text size

        // Add the TextView to the layout
        weatherEntriesLayout.addView(errorTextView)
    }

}