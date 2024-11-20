package com.nikita.weatherappui.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatDate(inputDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
            val date = inputFormat.parse(inputDate) ?: throw IllegalArgumentException("Invalid date format")
            outputFormat.format(date)
        } catch (e: Exception) {
            inputDate
        }
    }

    fun getCurrentHour(): Int {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }
}