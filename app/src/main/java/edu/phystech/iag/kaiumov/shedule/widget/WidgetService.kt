package edu.phystech.iag.kaiumov.shedule.widget

import android.content.Intent
import android.widget.RemoteViewsService
import edu.phystech.iag.kaiumov.shedule.Keys
import edu.phystech.iag.kaiumov.shedule.ScheduleApp
import edu.phystech.iag.kaiumov.shedule.Utils

class WidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        val timetable = (application as ScheduleApp).timetable!!
        val key = intent.getStringExtra(Keys.KEY)
        return WidgetAdapter(applicationContext, timetable[key]!!)
    }
}
