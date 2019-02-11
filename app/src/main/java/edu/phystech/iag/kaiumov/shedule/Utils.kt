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

    internal fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    internal fun loadKey(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Keys.PREF_GROUP, null)
    }

    internal fun modifyKey(context: Context, key: String) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putString(Keys.PREF_GROUP, key)
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
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, Keys.ENCODING))
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
                IOUtils.toString(context.assets.open(Keys.SCHEDULE_PATH), Keys.ENCODING),
                object : TypeToken<Schedule>() {}.type
        )
    }

    internal fun loadProfessors(context: Context): ArrayList<String> {
        return Gson().fromJson<ArrayList<String>>(
                IOUtils.toString(context.assets.open(Keys.PROFESSORS_PATH), Keys.ENCODING),
                object : TypeToken<ArrayList<String>>() {}.type
        )
    }
}
