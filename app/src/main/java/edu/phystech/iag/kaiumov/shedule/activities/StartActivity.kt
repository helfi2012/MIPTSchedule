package edu.phystech.iag.kaiumov.shedule.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import edu.phystech.iag.kaiumov.shedule.*
import kotlinx.android.synthetic.main.activity_start.*


class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val timetable = (application as ScheduleApp).timetable ?: return
        val adapter = ArrayAdapter<String>(applicationContext, R.layout.search_item)
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
            finish()
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
