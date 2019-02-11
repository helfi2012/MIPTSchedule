package edu.phystech.iag.kaiumov.shedule.model

import kotlin.math.abs

object TimeUtils {
    fun compareTime(t1: String, t2: String): Int {
        val s1 = t1.split(":").map { it -> it.toInt() }
        val s2 = t2.split(":").map { it -> it.toInt() }
        return (s1[0] - s2[0]) * 60 + s1[1] - s2[1]
    }

    fun length(t1: String, t2: String): Double {
        val s1 = t1.split(":").map { it -> it.toInt() }
        val s2 = t2.split(":").map { it -> it.toInt() }
        if (s1.size < 2 || s2.size < 2)
            return 1.0
        return abs((s2[0] - s1[0]) * 60 + s2[1] - s1[1]) / 60.0
    }
}