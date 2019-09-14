package edu.phystech.iag.kaiumov.shedule.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import edu.phystech.iag.kaiumov.shedule.DataUtils
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.ScheduleApp
import edu.phystech.iag.kaiumov.shedule.SearchTask
import edu.phystech.iag.kaiumov.shedule.notification.Alarm
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val timetable = (application as ScheduleApp).timetable
        val adapter = ArrayAdapter<String>(baseContext, R.layout.search_item)
        searchList.adapter = adapter

        searchView.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun afterTextChanged(p0: Editable?) {
                p0 ?: return
                SearchTask(searchView.text.toString(), timetable.keys.toList(), adapter).execute()
            }
        })

        val notifiedGroup = DataUtils.loadNotificationKey(applicationContext)
        searchList.setOnItemClickListener { _, _, position, _ ->
            val itemText = adapter.getItem(position)!!
            DataUtils.addKey(applicationContext, itemText)
            DataUtils.modifyMainKey(applicationContext, itemText)
            if (notifiedGroup == null) {
                DataUtils.modifyNotificationKey(applicationContext, itemText)
                Alarm.schedule(this)
            }
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        searchView.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        DataUtils.hideKeyboard(this)
    }
}
