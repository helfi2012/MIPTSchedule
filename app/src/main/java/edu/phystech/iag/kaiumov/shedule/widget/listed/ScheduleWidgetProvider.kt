package edu.phystech.iag.kaiumov.shedule.widget.listed

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import edu.phystech.iag.kaiumov.shedule.Keys
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.DataUtils


class ScheduleWidgetProvider : AppWidgetProvider() {

    private var page = Keys.DEFAULT_PAGE
    private var keys: List<String>? = null

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateViews(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        intent ?: return
        context ?: return
        if (intent.action == Keys.ACTION_SWITCH) {
            page = intent.getIntExtra(Keys.PAGE, Keys.DEFAULT_PAGE)
            var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateViews(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateViews(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        keys = DataUtils.loadKeys(context)
        setTextView(views)
        setListView(context, views, appWidgetId)
        setButton(context, views, appWidgetId, R.id.nextButton)
        setButton(context, views, appWidgetId, R.id.prevButton)
        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetListView)
    }

    private fun setTextView(views: RemoteViews) {
        views.setTextViewText(R.id.widgetTextBar, keys!![page])
    }

    private fun setListView(context: Context, views: RemoteViews, appWidgetId: Int) {
        val adapter = Intent(context, WidgetService::class.java)
        adapter.putExtra(Keys.KEY, keys!![page])
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        adapter.data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME))
        views.setRemoteAdapter(R.id.widgetListView, adapter)
        // Set pending intent to open activity on item click
//        val intent = Intent(context, MainActivity::class.java)
//        intent.putExtra(Keys.PAGE, page)
//        adapter.data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME))
//        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
//        views.setPendingIntentTemplate(R.id.widgetListView, pendingIntent)
    }

    private fun setButton(context: Context, views: RemoteViews, appWidgetId: Int, buttonId: Int) {
        val intent = Intent(context, ScheduleWidgetProvider::class.java)
        intent.action = Keys.ACTION_SWITCH
        intent.putExtra(Keys.PAGE, when {
            buttonId == R.id.nextButton -> (page + 1) % keys!!.size
            page - 1 < 0 -> keys!!.size - 1
            else -> page - 1
        })
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))
        val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent, 0)
        views.setOnClickPendingIntent(buttonId, pendingIntent)
    }
}
