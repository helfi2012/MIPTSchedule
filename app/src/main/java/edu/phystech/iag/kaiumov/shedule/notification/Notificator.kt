package edu.phystech.iag.kaiumov.shedule.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.activities.MainActivity
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils
import java.util.*
import kotlin.math.roundToInt

object Notificator {
    private const val NOTIFICATION_ID = 228
    // This is the Notification Channel ID
    private const val NOTIFICATION_CHANNEL_ID = "schedule_channel_id"
    //User visible Channel Name
    private const val CHANNEL_NAME = "Schedule Notifications"
    // Importance applicable to all the notifications in this Channel
    @RequiresApi(Build.VERSION_CODES.N)
    private const val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH

    fun buildNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, CHANNEL_NAME, IMPORTANCE)
                //Boolean value to set if vibration are enabled for Notifications from this Channel
                notificationChannel.vibrationPattern = longArrayOf(2000)
                notificationChannel.enableVibration(true)
                //Sets the color of Notification Light
                //Boolean value to set if lights are enabled for Notifications from this Channel
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.GREEN
                //Sets whether notifications from these Channel should be visible on Lockscreen or not
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }

    fun showNotification(context: Context, item: ScheduleItem) {
        // Load preferences
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val minutesBefore = ((TimeUtils.getCalendarTime(item.day, item.startTime).timeInMillis -
                Calendar.getInstance().timeInMillis) / 1000.0 / 60.0).roundToInt()

        val title = context.resources.getString(R.string.notification_title, item.name, item.startTime)
        var text = context.resources.getQuantityString(R.plurals.notification_text_time, minutesBefore, minutesBefore)
        if (item.place.isNotEmpty()) {
            text += context.resources.getString(R.string.notification_text_place, item.place)
        }
        // Pending intent
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_ONE_SHOT)
        // Notification Channel ID passed as a parameter here will be ignored for all the Android versions below 8.0
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.priority = NotificationCompat.PRIORITY_HIGH
        builder
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_add)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            val ringtone = preferences.getBoolean(context.resources.getString(R.string.pref_notification_ringtone_key), true)
            val vibrate = preferences.getBoolean(context.resources.getString(R.string.pref_notification_vibrate_key), true)
            builder.setDefaults(getDefaults(true, vibrate, ringtone))
        } else {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
    }

    private fun getDefaults(light: Boolean, vibrate: Boolean, sound: Boolean) : Int {
        var defaults = 0
        if (light) {
            defaults = defaults or NotificationCompat.DEFAULT_LIGHTS
        }
        if (vibrate) {
            defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
        }
        if (sound) {
            defaults = defaults or NotificationCompat.DEFAULT_SOUND
        }
        return defaults
    }
}