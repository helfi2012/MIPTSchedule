package edu.phystech.iag.kaiumov.schedule.model

import java.io.Serializable

class Schedule(val version: String, var timetable: HashMap<String, ArrayList<ScheduleItem>>) : Serializable