package edu.phystech.iag.kaiumov.shedule

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.phystech.iag.kaiumov.shedule.model.Schedule
import org.apache.commons.io.IOUtils
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader


object Utils {
    private const val ENCODING = "UTF-8"
    private const val DELIMITER = "|"

    internal fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    internal fun loadKeys(context: Context): List<String>? {
        val s = PreferenceManager.getDefaultSharedPreferences(context).getString(Keys.PREF_GROUP, null)
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
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(Keys.PREF_GROUP, if (keys.isEmpty()) null else s)
        editor.apply()
    }

    internal fun saveSchedule(context: Context, schedule: Schedule) {
        val outputStream = context.openFileOutput(Keys.SCHEDULE_PATH, Context.MODE_PRIVATE)
        val json = Gson().toJson(schedule)
        outputStream.write(json.toByteArray())
        outputStream.close()
    }

    internal fun loadSchedule(context: Context): Schedule {
        try {
            val inputStream = context.openFileInput(Keys.SCHEDULE_PATH)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, ENCODING))
            var output = ""
            while (true) {
                val local = bufferedReader.readLine() ?: break
                output += local
            }
            bufferedReader.close()
            inputStream.close()
            return Gson().fromJson<Schedule>(output, object : TypeToken<Schedule>() {}.type)
        } catch (e: FileNotFoundException) {
            val schedule = loadScheduleFromAssets(context)
            saveSchedule(context, schedule)
            return schedule
        }
    }

    internal fun loadScheduleFromAssets(context: Context): Schedule {
        return Gson().fromJson<Schedule>(
                IOUtils.toString(context.assets.open(Keys.SCHEDULE_PATH), ENCODING),
                object : TypeToken<Schedule>() {}.type
        )
    }
}
