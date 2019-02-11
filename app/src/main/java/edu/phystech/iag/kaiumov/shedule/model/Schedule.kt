package edu.phystech.iag.kaiumov.shedule.model

import java.io.Serializable

class Schedule(val version: String, var timetable: HashMap<String, ArrayList<ScheduleItem>>) : Serializable