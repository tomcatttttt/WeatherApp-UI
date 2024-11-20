package com.nikita.weatherappui.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.nikita.weatherappui.R
import com.nikita.weatherappui.model.HourlyForecast

class HourlyForecastAdapter(
    private var forecastList: List<HourlyForecast>,
    private var cityLocalTime: String,
    private val onItemClick: (HourlyForecast) -> Unit
) : RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

    private var selectedPosition: Int = -1
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.hourlyItemContainer)
        val weatherIcon: ImageView = itemView.findViewById(R.id.weatherIcon)
        val hourText: TextView = itemView.findViewById(R.id.hourText)
        val tempText: TextView = itemView.findViewById(R.id.tempText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast = forecastList[position]

        holder.weatherIcon.load(forecast.iconUrl)
        holder.hourText.text = forecast.time
        holder.tempText.text = "${forecast.temperature}°"

        val currentCityHour = cityLocalTime.substringBefore(":")
        val forecastHour = forecast.time.substringBefore(":")
        if (selectedPosition == -1 && currentCityHour == forecastHour) {
            selectedPosition = position
        }

        if (selectedPosition == position) {
            holder.container.setBackgroundResource(R.drawable.now_time_select)
        } else {
            holder.container.setBackgroundResource(android.R.color.transparent)
        }

        holder.container.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(position)

            onItemClick(forecast)
        }
    }

    override fun getItemCount(): Int = forecastList.size

    fun updateData(newData: List<HourlyForecast>, updatedCityLocalTime: String) {
        forecastList = newData
        cityLocalTime = updatedCityLocalTime
        selectedPosition = -1
        notifyDataSetChanged()
    }
}