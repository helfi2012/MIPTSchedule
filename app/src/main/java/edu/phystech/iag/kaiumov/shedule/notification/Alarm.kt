package edu.phystech.iag.kaiumov.shedule.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import edu.phystech.iag.kaiumov.shedule.Keys
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.Utils
import edu.phystech.iag.kaiumov.shedule.model.Schedule
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils
import java.util.*
import kotlin.random.Random
import android.app.PendingIntent
import edu.phystech.iag.kaiumov.shedule.activities.MainActivity


// https://developer.android.com/training/scheduling/alarms

object Alarm {

    private const val INTERVAL_WEEK = 7 * 24 * 3600 * 1000L
    
    class AlarmNotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val notificationEnabled = preferences.getBoolean(context.resources.getString(R.string.pref_notification_enabled_key), true)
            if (notificationEnabled) {
                val scheduleItem = intent.getBundleExtra(Keys.ITEM).getSerializable(Keys.ITEM) as ScheduleItem
                showNotification(context, scheduleItem)
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
        // Cancel previous notifications
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
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    time.timeInMillis,
                    INTERVAL_WEEK,
                    pendingIntent
            )
        }
    }

    fun schedule(context: Context, schedule: Schedule) {
        val keys = Utils.loadKeys(context)
        if (keys == null) {
            resetAlarm(context)
            return
        }
        schedule(context, schedule, keys[0])
    }
    
    fun schedule(context: Context) {
        val keys = Utils.loadKeys(context)
        if (keys == null) {
            resetAlarm(context)
            return
        }
        val schedule = Utils.loadSchedule(context)
        schedule(context, schedule, keys[0])
    }

    // This is the Notification Channel ID
    private const val NOTIFICATION_CHANNEL_ID = "schedule_channel_id"
    //User visible Channel Name
    private const val CHANNEL_NAME = "Schedule Notifications"
    // Importance applicable to all the notifications in this Channel
    @RequiresApi(Build.VERSION_CODES.N)
    private const val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    // Pattern is in milliseconds with the format {delay,play,sleep,play,sleep...}
    private val VIBRATE_PATTERN = longArrayOf(1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000)

    fun buildNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, IMPORTANCE)
                //Boolean value to set if lights are enabled for Notifications from this Channel
                notificationChannel.enableLights(true)
                //Boolean value to set if vibration are enabled for Notifications from this Channel
                notificationChannel.enableVibration(true)
                //Sets the color of Notification Light
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.vibrationPattern = VIBRATE_PATTERN
                //Sets whether notifications from these Channel should be visible on Lockscreen or not
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }

    fun showNotification(context: Context, scheduleItem: ScheduleItem) {
        // Load preferences
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val minutesBefore = preferences.getString(context.resources.getString(R.string.pref_notification_before_key), "5")!!.toInt()
        val ringtone = preferences.getString(context.resources.getString(R.string.pref_notification_ringtone_key), "")
        val vibrate = preferences.getBoolean(context.resources.getString(R.string.pref_notification_vibrate_key), true)
        var text = "Через $minutesBefore минут"
        if (scheduleItem.place.isNotEmpty()) {
            text += " в " + scheduleItem.place
        }
        // Pending intent
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        // Notification Channel ID passed as a parameter here will be ignored for all the Android versions below 8.0
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        if (vibrate) {
            builder.setVibrate(VIBRATE_PATTERN)
        }
        builder
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setSound(Uri.parse(ringtone))
            .setContentTitle(scheduleItem.name)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_add)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(Random.nextInt(), builder.build())
    }
}