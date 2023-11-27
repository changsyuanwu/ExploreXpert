package com.example.explorexpert.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.R

class ForecastAdapter(private val forecastItems: ArrayList<ForecastItem>) :
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
        private val weatherConditionTextView: TextView = itemView.findViewById(R.id.weatherConditionTextView)

        fun bind(item: ForecastItem) {
            // Bind data to views
            dateTextView.text = item.date
            temperatureTextView.text = item.temperature
            weatherConditionTextView.text = item.weatherCondition
        }
    }

    data class ForecastItem(val date: String, val temperature: String, val weatherCondition: String)
}
