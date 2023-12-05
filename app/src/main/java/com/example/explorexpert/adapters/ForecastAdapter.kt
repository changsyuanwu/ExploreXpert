package com.example.explorexpert.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.R
import java.io.IOException
import java.io.InputStream

class ForecastAdapter(private val forecastItems: ArrayList<ForecastItem>, private val context: Context) :
    RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weather_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = forecastItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return forecastItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val temperatureTextView: TextView = itemView.findViewById(R.id.temperatureTextView)
        private val weatherConditionTextView: TextView =
            itemView.findViewById(R.id.weatherConditionTextView)
        private val weatherIconImageView: ImageView = itemView.findViewById(R.id.weatherIconImageView)
        val lineView = itemView.findViewById<View>(R.id.divider)

        fun bind(item: ForecastItem) {
            // Bind data to views
            dateTextView.text = item.date
            temperatureTextView.text = item.temperature
            weatherConditionTextView.text = item.weatherCondition
            loadWeatherIcon(weatherIconImageView, item.weatherCondition)
            if (position == forecastItems.size - 1) {
                lineView.visibility = View.GONE
            }
        }
    }
    private fun loadWeatherIcon(imageView: ImageView, weatherStatus: String) {
        Log.i("TAG", "Weather status received: '$weatherStatus'")
        val imageName: String = when (weatherStatus) {
            "Thunderstorm" -> "Thundery.png"
            "Clouds" -> "Cloudy.png"
            "Rain" -> "Rainy.png"
            "Snow" -> "Snowy.png"
            // Add more cases as needed for other weather statuses
            else -> "Sunny.png"
        }
        try {
            // Load the image from the assets folder
            val inputStream: InputStream = context.assets.open(imageName)
            val drawable = Drawable.createFromStream(inputStream, null)
            imageView.setImageDrawable(drawable)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    data class ForecastItem(
        val date: String,
        val temperature: String,
        val weatherCondition: String,
        val weatherIcon: String
    )


}
