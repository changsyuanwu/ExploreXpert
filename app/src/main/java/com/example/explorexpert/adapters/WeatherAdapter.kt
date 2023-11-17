package com.example.explorexpert.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.R


class WeatherAdapter(private val weatherItems: List<WeatherItem>) :
    RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.weather_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = weatherItems[position]
        holder.dateTextView.text = item.date
        holder.temperatureTextView.text = item.temperature
    }

    override fun getItemCount(): Int {
        return weatherItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val temperatureTextView: TextView = itemView.findViewById(R.id.temperatureTextView)
    }

    data class WeatherItem( val date: String,  val temperature: String) {
//
//        fun getDate(): String {
//            return date
//        }
//
//        fun getTemperature(): String {
//            return temperature
//        }
    }


}
