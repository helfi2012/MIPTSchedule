package edu.phystech.iag.kaiumov.shedule.activities

import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import edu.phystech.iag.kaiumov.shedule.*
import edu.phystech.iag.kaiumov.shedule.notification.Alarm
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val nightMode = preferences.getBoolean(getString(R.string.pref_night_key), false)
        if (nightMode) {
            setTheme(R.style.AppTheme_Dark)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val timetable = (application as ScheduleApp).timetable
        var adapter = ArrayAdapter<String>(applicationContext, R.layout.search_item_day)
        if (nightMode) {
            adapter = ArrayAdapter(applicationContext, R.layout.search_item_night)
        }
        searchList.adapter = adapter

        searchView.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun afterTextChanged(p0: Editable?) {
                p0 ?: return
                SearchTask(searchView.text.toString(), timetable.keys.toList(), Keys.SEARCH_LIMIT, adapter).execute()
            }
        })

        searchList.setOnItemClickListener { _, _, position, _ ->
            Utils.addKey(applicationContext, adapter.getItem(position)!!)
            Alarm.schedule(this)
            finish()
        }

        if (nightMode) {
            imageView.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        searchView.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        Utils.hideKeyboard(this)
    }
}
