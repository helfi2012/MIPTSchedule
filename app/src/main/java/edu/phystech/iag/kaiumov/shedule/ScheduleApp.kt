package edu.phystech.iag.kaiumov.shedule

import android.app.Application
import android.os.AsyncTask
import edu.phystech.iag.kaiumov.shedule.model.Schedule
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.notification.Alarm

class ScheduleApp : Application() {

    private lateinit var schedule: Schedule
    val timetable: HashMap<String, ArrayList<ScheduleItem>>
        get() = schedule.timetable

    override fun onCreate() {
        super.onCreate()
        // Load schedule from memory
        schedule = Utils.loadSchedule(applicationContext)
        // If there is new version in assets, update schedule in memory
        val initialSchedule = Utils.loadScheduleFromAssets(applicationContext)
        if (schedule.version != initialSchedule.version) {
            schedule = initialSchedule
            Utils.saveSchedule(applicationContext, schedule)
        }
        Alarm.buildNotificationChannel(this)
    }

    fun resetSchedule() {
        AsyncTask.execute {
            schedule = Utils.loadScheduleFromAssets(applicationContext)
            Utils.saveSchedule(applicationContext, schedule)
            Alarm.schedule(applicationContext, schedule)
        }
    }

    fun updateTimeTable(timetable: HashMap<String, ArrayList<ScheduleItem>>) {
        schedule.timetable = timetable
        AsyncTask.execute {
            Utils.saveSchedule(applicationContext, schedule)
            Alarm.schedule(applicationContext, schedule)
        }
    }
}