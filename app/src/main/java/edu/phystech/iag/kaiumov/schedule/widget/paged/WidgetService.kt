package edu.phystech.iag.kaiumov.schedule.widget.paged

import android.content.Intent
import android.widget.RemoteViewsService
import edu.phystech.iag.kaiumov.schedule.utils.DataUtils
import edu.phystech.iag.kaiumov.schedule.Keys
import edu.phystech.iag.kaiumov.schedule.ScheduleApp

class WidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val timetable = (application as ScheduleApp).timetable
        val key = DataUtils.loadMainKey(baseContext)
        val day = intent.getIntExtra(Keys.DAY, Keys.DEFAULT_PAGE)
        return WidgetAdapter(applicationContext, day, timetable[key]!!)
    }

}