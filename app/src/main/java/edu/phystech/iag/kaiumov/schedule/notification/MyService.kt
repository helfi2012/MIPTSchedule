package edu.phystech.iag.kaiumov.schedule.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder


class MyService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onCreate() {
        super.onCreate()
        Alarm.schedule(this)
    }

}