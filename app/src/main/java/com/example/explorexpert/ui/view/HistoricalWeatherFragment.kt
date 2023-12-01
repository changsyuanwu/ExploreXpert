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
import com.android.volley.Request
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
import java.util.Date

class HistoricalWeatherFragment : Fragment() {
    private lateinit var weatherEntriesLayout: LinearLayout
    private lateinit var appInfo: ApplicationInfo

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appInfo = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historical_weather, container, false)
        weatherEntriesLayout = view.findViewById(R.id.weatherEntriesLayout)

        // Retrieve latitude, longitude, start date, and end date from arguments
        val latitude = arguments?.getDouble("latitude", 0.0) ?: 0.0
        val longitude = arguments?.getDouble("longitude", 0.0) ?: 0.0
        val startDate = convertDateToUnix(arguments?.getString("startDate", "") ?: "")
        val endDate = convertDateToUnix(arguments?.getString("endDate", "") ?: "")

        // Fetch historical weather data
        fetchHistoricalWeatherData(latitude, longitude, startDate, endDate)

        return view
    }

    private fun fetchHistoricalWeatherData(latitude: Double, longitude: Double, startDate: Long, endDate: Long) {
        val apiKey = appInfo.metaData.getString("openweather.API_KEY")
        val historicalWeatherUrl =
            "https://history.openweathermap.org/data/2.5/history/city?lat=$latitude&lon=$longitude&start=$startDate&end=$endDate&units=metric&appid=$apiKey"

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

                // Check if the date already exists in the set before creating the entry
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

}