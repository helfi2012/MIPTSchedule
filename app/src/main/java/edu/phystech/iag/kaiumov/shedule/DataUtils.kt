package edu.phystech.iag.kaiumov.shedule

import android.app.Activity
import android.content.Context
import androidx.preference.PreferenceManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.phystech.iag.kaiumov.shedule.model.Schedule
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader


object DataUtils {
    private const val ENCODING = "UTF-8"
    private const val DELIMITER = "|"
    private const val SCHEDULE_PATH = "schedule.json"
    private const val PROF_PATH = "prof.json"

    internal fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    internal fun loadProfessorsList(context: Context) : ArrayList<String> {
        return Gson().fromJson(
                IOUtils.toString(context.assets.open(PROF_PATH), ENCODING),
                object : TypeToken<ArrayList<String>>() {}.type
        )
    }

    internal fun loadMainKey(context: Context) : String? {
        val key = context.resources.getString(R.string.pref_main_group_key)
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
    }

    internal fun modifyMainKey(context: Context, group: String) {
        val key = context.resources.getString(R.string.pref_main_group_key)
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(key, group)
        editor.apply()
    }

    internal fun loadNotificationKey(context: Context) : String? {
        val key = context.resources.getString(R.string.pref_notification_group_key)
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
    }

    internal fun modifyNotificationKey(context: Context, group: String) {
        val key = context.resources.getString(R.string.pref_notification_group_key)
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(key, group)
        editor.apply()
    }

    internal fun loadKeys(context: Context): List<String>? {
        val key = context.resources.getString(R.string.pref_groups_key)
        val s = PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
        return s?.split(DELIMITER)
    }

    internal fun addKey(context: Context, key: String) {
        val keys = (loadKeys(context) ?: List(0) { "" }).toMutableList()
        keys.add(key)
        modifyKeys(context, keys)
    }

    internal fun modifyKeys(context: Context, keys: List<String>) {
        var s = ""
        for (i in 0 until keys.size) {
            s += keys[i]
            if (i < keys.size - 1)
                s += DELIMITER
        }
        val key = context.resources.getString(R.string.pref_groups_key)
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(key, if (keys.isEmpty()) null else s)
        editor.apply()
    }

    internal fun saveSchedule(context: Context, schedule: Schedule) {
        val outputStream = context.openFileOutput(SCHEDULE_PATH, Context.MODE_PRIVATE)
        val json = Gson().toJson(schedule)
        outputStream.write(json.toByteArray())
        outputStream.close()
    }

    internal fun loadSchedule(context: Context): Schedule {
        try {
            return Gson().fromJson(
                    IOUtils.toString(context.openFileInput(SCHEDULE_PATH), ENCODING),
                    object : TypeToken<Schedule>() {}.type
            )
        } catch (e: FileNotFoundException) {
            val schedule = loadScheduleFromAssets(context)
            saveSchedule(context, schedule)
            return schedule
        }
    }

    internal fun loadScheduleFromAssets(context: Context): Schedule {
        return Gson().fromJson(
                IOUtils.toString(context.assets.open(SCHEDULE_PATH), ENCODING),
                object : TypeToken<Schedule>() {}.type
        )
    }
}
