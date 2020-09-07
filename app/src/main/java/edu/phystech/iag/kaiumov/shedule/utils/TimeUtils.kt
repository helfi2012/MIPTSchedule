package edu.phystech.iag.kaiumov.shedule.utils

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    /**
     * @return positive if t1 > t2, negative if t1 < t2, zero if t1 == t2
     */
    fun compareTime(t1: String, t2: String): Int {
        val parser =  SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return try {
            (parser.parse(t1)!!.time - parser.parse(t2)!!.time).toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun length(t1: String, t2: String): Double {
        val parser =  SimpleDateFormat("HH:mm", Locale.ENGLISH)
        return try {
            (parser.parse(t2)!!.time - parser.parse(t1)!!.time) / 1000 / 3600.0
        } catch (e: Exception) {
            1.0
        }
    }

    fun getCalendarTime(day: Int, t: String): Calendar {
        val calendar = Calendar.getInstance()
        val split = t.split(":")
        calendar.set(Calendar.DAY_OF_WEEK, if (day in 1..5) day + 1 else day - 6)
        calendar.set(Calendar.HOUR_OF_DAY, split[0].toInt())
        calendar.set(Calendar.MINUTE, split[1].toInt())
        return calendar
    }

    /**
     * @return day number from Monday - 1 to Sunday - 7
     */
    fun getCurrentDay(): Int {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 2
        return if (currentDay in 0..6) currentDay + 1 else 7
    }

    fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.HOUR_OF_DAY).toString() + ":" + calendar.get(Calendar.MINUTE)
    }
}