package edu.phystech.iag.kaiumov.shedule.model

import java.io.Serializable

class ScheduleItem(val name: String,
                   val prof: String,
                   val place: String,
                   val day: Int,
                   val type: String,
                   val startTime: String,
                   val endTime: String,
                   val notes: String) : Serializable {

    fun length(): Double = TimeUtils.length(startTime, endTime)

    override fun equals(other: Any?): Boolean {
        val that = other as ScheduleItem
        return (this.name == that.name) && (this.day == that.day) && (this.startTime == that.startTime)
    }

    override fun toString(): String {
        return "name: ${this.name}, prof: ${this.prof}, place: ${this.place}\n, day: ${this.day}"
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + prof.hashCode()
        result = 31 * result + place.hashCode()
        result = 31 * result + day
        result = 31 * result + type.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + notes.hashCode()
        return result
    }
}