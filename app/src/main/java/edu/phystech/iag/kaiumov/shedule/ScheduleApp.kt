package edu.phystech.iag.kaiumov.shedule

import android.app.Application
import android.os.AsyncTask
import androidx.preference.PreferenceManager
import edu.phystech.iag.kaiumov.shedule.model.Schedule
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.notification.Alarm
import edu.phystech.iag.kaiumov.shedule.notification.Notificator


class ScheduleApp : Application() {

    private lateinit var schedule: Schedule
    val timetable: HashMap<String, ArrayList<ScheduleItem>>
        get() = schedule.timetable

    override fun onCreate() {
        super.onCreate()
        // Load schedule from memory
        schedule = DataUtils.loadSchedule(applicationContext)
        // If there is new version in assets, update schedule in memory
        val initialSchedule = DataUtils.loadScheduleFromAssets(applicationContext)
        if (schedule.version != initialSchedule.version) {
            schedule = initialSchedule
            DataUtils.saveSchedule(applicationContext, schedule)
        }
        Notificator.buildNotificationChannel(this)
        Alarm.schedule(this)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePref = sharedPreferences.getString(getString(R.string.pref_theme_key), ThemeHelper.DEFAULT_MODE)
        ThemeHelper.applyTheme(themePref!!)
    }

    fun resetSchedule() {
        AsyncTask.execute {
            schedule = DataUtils.loadScheduleFromAssets(applicationContext)
            DataUtils.saveSchedule(applicationContext, schedule)
            Alarm.schedule(applicationContext, schedule)
        }
    }

    fun updateTimeTable(timetable: HashMap<String, ArrayList<ScheduleItem>>) {
        schedule.timetable = timetable
        AsyncTask.execute {
            DataUtils.saveSchedule(applicationContext, schedule)
            Alarm.schedule(applicationContext, schedule)
        }
    }
}