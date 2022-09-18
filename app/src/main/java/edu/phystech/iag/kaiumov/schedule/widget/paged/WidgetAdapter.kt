package edu.phystech.iag.kaiumov.schedule.widget.paged

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import androidx.core.content.ContextCompat
import edu.phystech.iag.kaiumov.schedule.Keys
import edu.phystech.iag.kaiumov.schedule.utils.ColorUtil
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.schedule.ui.activities.MainActivity
import edu.phystech.iag.kaiumov.schedule.model.ScheduleItem

class WidgetAdapter(private val context: Context,
                    private val day: Int,
                    lessons: List<ScheduleItem>) : RemoteViewsFactory {

    private val data = lessons.filter { it.day == day }

    override fun onCreate() = Unit

    override fun getCount(): Int = data.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewAt(position: Int): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_item)
        val item = data[position]
        remoteViews.setTextViewText(R.id.name, item.name)
        remoteViews.setTextViewText(R.id.place, item.place)
        remoteViews.setTextViewText(R.id.startTime, item.startTime)
        remoteViews.setTextViewText(R.id.endTime, item.endTime)
        remoteViews.setInt(R.id.time_layout, "setBackgroundResource", ColorUtil.getBackgroundDrawable(item.type))
        remoteViews.setInt(R.id.timeLine, "setBackgroundResource", ColorUtil.getTextColor(item.type))
        val textColor = ContextCompat.getColor(context, ColorUtil.getTextColor(item.type))
        remoteViews.setTextColor(R.id.startTime, textColor)
        remoteViews.setTextColor(R.id.endTime, textColor)

        val clickIntent = Intent(context, MainActivity::class.java)
        clickIntent.putExtra(Keys.PAGE, day - 1)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getService(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getService(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        remoteViews.setOnClickPendingIntent(R.id.scheduleMainLayout, pendingIntent)
        return remoteViews
    }

    override fun getViewTypeCount(): Int = 1

    override fun hasStableIds(): Boolean = true

    override fun onDataSetChanged() = Unit

    override fun onDestroy() = Unit

}