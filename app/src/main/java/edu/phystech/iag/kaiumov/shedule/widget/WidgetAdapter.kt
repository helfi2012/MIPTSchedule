package edu.phystech.iag.kaiumov.shedule.widget


import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import edu.phystech.iag.kaiumov.shedule.schedule_ui.RecyclerAdapter
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils

class WidgetAdapter(private val context: Context, classes: List<ScheduleItem>) : RemoteViewsFactory {

    // Special class that includes Headers
    class ListItem(var scheduleItem: ScheduleItem? = null, var day: Int = 0)

    private val data = ArrayList<ListItem>()

    init {
        val day = TimeUtils.getCurrentDay()
        for (i in 0 until classes.size) {
            if (classes[i].day < day)
                continue
            if (isFirstLesson(classes, i)) {
                data.add(ListItem(day = classes[i].day))
            }
            data.add(ListItem(scheduleItem = classes[i]))
        }
        // if there's no any lessons on this week - show next week
        if (data.isEmpty()) {
            for (i in 0 until classes.size) {
                if (isFirstLesson(classes, i)) {
                    data.add(ListItem(day = classes[i].day))
                }
                data.add(ListItem(scheduleItem = classes[i]))
            }
        }
    }

    private fun isFirstLesson(classes: List<ScheduleItem>, index: Int) : Boolean {
        for (lesson in classes.filter { classes[index].day == it.day }) {
            if (TimeUtils.compareTime(lesson.startTime, classes[index].startTime) < 0) {
                return false
            }
        }
        return true
    }

    override fun onCreate() = Unit

    override fun getCount(): Int = data.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewAt(position: Int): RemoteViews {
        val listItem = data[position]
        if (listItem.scheduleItem == null) {
            val remoteViews = RemoteViews(context.packageName, R.layout.recycler_header)
            remoteViews.setTextViewText(R.id.headerText,
                    context.resources.getStringArray(R.array.week)[listItem.day - 1].toString())
            return remoteViews
        }
        val remoteViews = RemoteViews(context.packageName, R.layout.schedule_item)
        val item = listItem.scheduleItem!!
        remoteViews.setTextViewText(R.id.name, item.name)
        remoteViews.setTextViewText(R.id.prof, item.prof)
        remoteViews.setTextViewText(R.id.place, item.place)
        remoteViews.setTextViewText(R.id.startTime, item.startTime)
        remoteViews.setTextViewText(R.id.endTime, item.endTime)
        remoteViews.setInt(R.id.time_layout,
                "setBackgroundResource",
                RecyclerAdapter.lessonDrawable(item.type))
        val clickIntent = Intent()
        remoteViews.setOnClickFillInIntent(R.id.scheduleMainLayout, clickIntent)
        return remoteViews
    }

    override fun getViewTypeCount(): Int = 2

    override fun hasStableIds(): Boolean = true

    override fun onDataSetChanged() = Unit

    override fun onDestroy() = Unit
}
