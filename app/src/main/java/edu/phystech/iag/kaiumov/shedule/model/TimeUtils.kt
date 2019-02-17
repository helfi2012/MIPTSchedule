package edu.phystech.iag.kaiumov.shedule.model

import java.util.*
import kotlin.math.abs

object TimeUtils {
    /**
     * @return positive if t1 > t2, negative if t1 < t2, zero if t1 == t2
     */
    fun compareTime(t1: String, t2: String): Int {
        val s1 = t1.split(":").map { it.toInt() }
        val s2 = t2.split(":").map { it.toInt() }
        return (s1[0] - s2[0]) * 60 + s1[1] - s2[1]
    }

    fun length(t1: String, t2: String): Double {
        val s1 = t1.split(":").map { it.toInt() }
        val s2 = t2.split(":").map { it.toInt() }
        if (s1.size < 2 || s2.size < 2)
            return 1.0
        return abs((s2[0] - s1[0]) * 60 + s2[1] - s1[1]) / 60.0
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