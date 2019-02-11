package edu.phystech.iag.kaiumov.shedule

import android.app.Activity
import android.app.Application
import android.os.AsyncTask
import edu.phystech.iag.kaiumov.shedule.model.Schedule
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem

class ScheduleApp : Application() {

    private var schedule: Schedule? = null
    var timetable: HashMap<String, ArrayList<ScheduleItem>>? = null
            get() = schedule?.timetable

    override fun onCreate() {
        super.onCreate()
        // Load schedule from memory
        schedule = Utils.loadSchedule(applicationContext)
        // If there is new version in assets, update schedule in memory
        val initialSchedule = Utils.loadScheduleFromAssets(applicationContext)
        if (schedule!!.version != initialSchedule.version) {
            schedule = initialSchedule
            Utils.saveSchedule(applicationContext, schedule!!)
        }
    }

    fun resetSchedule(activity: Activity, onPostExecute: () -> Unit) {
        AsyncTask.execute {
            schedule = Utils.loadScheduleFromAssets(applicationContext)
            Utils.saveSchedule(applicationContext, schedule!!)
            activity.runOnUiThread {
                onPostExecute.invoke()
            }
        }
    }

    fun updateTimeTable(timetable: HashMap<String, ArrayList<ScheduleItem>>) {
        schedule!!.timetable = timetable
        AsyncTask.execute {
            Utils.saveSchedule(applicationContext, schedule!!)
        }
    }
}