package edu.phystech.iag.kaiumov.shedule.widget.paged

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import androidx.core.content.ContextCompat
import edu.phystech.iag.kaiumov.shedule.ColorUtil
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem

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
        val clickIntent = Intent()
        remoteViews.setOnClickFillInIntent(R.id.scheduleMainLayout, clickIntent)
        return remoteViews
    }

    override fun getViewTypeCount(): Int = 2

    override fun hasStableIds(): Boolean = true

    override fun onDataSetChanged() = Unit

    override fun onDestroy() = Unit

}