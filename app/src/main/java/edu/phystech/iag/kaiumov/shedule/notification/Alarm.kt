package edu.phystech.iag.kaiumov.shedule.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import edu.phystech.iag.kaiumov.shedule.DataUtils
import edu.phystech.iag.kaiumov.shedule.Keys
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.model.Schedule
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils
import java.util.*


// https://developer.android.com/training/scheduling/alarms

object Alarm {

    private const val INTERVAL_WEEK = 7 * 24 * 3600 * 1000L
    
    class AlarmNotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val notificationEnabled = preferences.getBoolean(context.resources.getString(R.string.pref_notification_enabled_key), false)
            // display
            if (notificationEnabled) {
                val scheduleItem = intent.getBundleExtra(Keys.ITEM)?.getSerializable(Keys.ITEM) as ScheduleItem
                Notificator.showNotification(context, scheduleItem)
            }
        }
    }

    // Cancel previous notifications
    private fun resetAlarm(context: Context, alarmManager: AlarmManager, preferences: SharedPreferences) {
        val notSize = preferences.getInt(context.resources.getString(R.string.pref_notification_size_key), 0)
        for (i in 0 until notSize) {
            val intent = Intent(context, AlarmNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            alarmManager.cancel(pendingIntent)
        }
    }

    // Cancel previous notifications
    private fun resetAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        resetAlarm(context, alarmManager, preferences)
    }

    fun schedule(context: Context, schedule: Schedule, key: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val minutesBefore = preferences.getString(context.resources.getString(R.string.pref_notification_before_key), "5")!!.toInt()
        val items = schedule.timetable[key]!!
        // Cancel previous alarms
        resetAlarm(context, alarmManager, preferences)
        // Save new notification queue size
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putInt(context.resources.getString(R.string.pref_notification_size_key), items.size)
        editor.apply()
        // Create new notifications queue
        for (i in 0 until items.size) {
            val item = items[i]
            val intent = Intent(context, AlarmNotificationReceiver::class.java)
            val bundle = Bundle()
            bundle.putSerializable(Keys.ITEM, item)
            intent.putExtra(Keys.ITEM, bundle)
            val pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val time = TimeUtils.getCalendarTime(item.day, item.startTime)
            val now = Calendar.getInstance()
            if (time.timeInMillis - now.timeInMillis < 0) {
                time.add(Calendar.DAY_OF_YEAR, 7)
            }
            time.add(Calendar.MINUTE, -minutesBefore)
            time.add(Calendar.MINUTE, -1)

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)
//            } else {
//                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.timeInMillis, pendingIntent)
//            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.timeInMillis, INTERVAL_WEEK, pendingIntent)
        }
    }

    fun schedule(context: Context, schedule: Schedule) {
        val key = DataUtils.loadNotificationKey(context)
        if (key == null) {
            resetAlarm(context)
            return
        }
        schedule(context, schedule, key)
    }
    
    fun schedule(context: Context) {
        val key = DataUtils.loadNotificationKey(context)
        if (key == null) {
            resetAlarm(context)
            return
        }
        val schedule = DataUtils.loadSchedule(context)
        schedule(context, schedule, key)
    }
}