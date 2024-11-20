package com.nikita.weatherappui.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nikita.weatherappui.R
import com.nikita.weatherappui.model.NextDayForecast

class NextDaysForecastAdapter(
    private var forecastList: List<NextDayForecast>
) : RecyclerView.Adapter<NextDaysForecastAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfWeek: TextView = itemView.findViewById(R.id.dayOfWeek)
        val weatherIcon: ImageView = itemView.findViewById(R.id.weatherIcon)
        val maxTemp: TextView = itemView.findViewById(R.id.maxTemp)
        val minTemp: TextView = itemView.findViewById(R.id.minTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_next_day_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast = forecastList[position]
        holder.dayOfWeek.text = forecast.dayOfWeek
        holder.maxTemp.text = "${forecast.maxTemp}°C"
        holder.minTemp.text = "${forecast.minTemp}°C"
        holder.weatherIcon.load(forecast.iconUrl)
    }

    override fun getItemCount(): Int = forecastList.size

    fun updateData(newData: List<NextDayForecast>) {
        forecastList = newData
        notifyDataSetChanged()
    }
}