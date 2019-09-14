package edu.phystech.iag.kaiumov.shedule.widget.paged

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import edu.phystech.iag.kaiumov.shedule.Keys
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.activities.MainActivity
import java.util.*


class ScheduleWidgetProvider : AppWidgetProvider() {

    private var day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        day = if (day in 0..6) day else 6
        for (appWidgetId in appWidgetIds) {
            updateViews(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        intent ?: return
        context ?: return
        if (intent.action == Keys.ACTION_SWITCH) {
            day = intent.getIntExtra(Keys.DAY, Keys.DEFAULT_PAGE)
            var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateViews(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateViews(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        setTextView(context, views)
        setListView(context, views, appWidgetId)
        setButton(context, views, appWidgetId, R.id.nextButton)
        setButton(context, views, appWidgetId, R.id.prevButton)
        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetListView)
    }

    private fun setTextView(context: Context, views: RemoteViews) {
        views.setTextViewText(R.id.widgetTextBar, context.resources.getStringArray(R.array.week)[day].toString())
    }

    private fun setListView(context: Context, views: RemoteViews, appWidgetId: Int) {
        val adapter = Intent(context, WidgetService::class.java)
        adapter.putExtra(Keys.DAY, day + 1)
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        adapter.data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME))
        views.setRemoteAdapter(R.id.widgetListView, adapter)
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(Keys.DAY, day + 1)
        adapter.data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME))
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        views.setPendingIntentTemplate(R.id.widgetListView, pendingIntent)
    }

    private fun setButton(context: Context, views: RemoteViews, appWidgetId: Int, buttonId: Int) {
        val intent = Intent(context, ScheduleWidgetProvider::class.java)
        intent.action = Keys.ACTION_SWITCH
        intent.putExtra(Keys.DAY, when {
            buttonId == R.id.nextButton -> (day + 1) % 7
            day - 1 < 0 -> day + 6
            else -> day - 1
        })
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0)
        views.setOnClickPendingIntent(buttonId, pendingIntent)
    }
}