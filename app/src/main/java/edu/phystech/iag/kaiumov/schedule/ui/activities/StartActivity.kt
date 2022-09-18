package edu.phystech.iag.kaiumov.schedule.ui.activities

import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import edu.phystech.iag.kaiumov.schedule.utils.DataUtils
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.schedule.ScheduleApp
import edu.phystech.iag.kaiumov.shedule.databinding.ActivityStartBinding
import edu.phystech.iag.kaiumov.schedule.notification.Alarm
import me.xdrop.fuzzywuzzy.FuzzySearch
import kotlin.collections.ArrayList


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val timetable = (application as ScheduleApp).timetable
        val adapter = ArrayAdapter<String>(baseContext, R.layout.search_item)
        binding.searchList.adapter = adapter

        val keys = timetable.keys.toList()
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun afterTextChanged(p0: Editable?) {
                p0 ?: return
                SearchTask(binding.searchView.text.toString(), keys, adapter).execute()
            }
        })

        val notifiedGroup = DataUtils.loadNotificationKey(applicationContext)
        binding.searchList.setOnItemClickListener { _, _, position, _ ->
            val itemText = adapter.getItem(position)!!
            DataUtils.addKey(applicationContext, itemText)
            DataUtils.modifyMainKey(applicationContext, itemText)
            if (notifiedGroup == null) {
                DataUtils.modifyNotificationKey(applicationContext, itemText)
                Alarm.schedule(this)
            }
            finish()
        }

        // Creating new group
        binding.createButton.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_create, null)
            val editText = view.findViewById<EditText>(R.id.editText)
            val textInputLayout = editText.parent.parent as TextInputLayout
            var isError = false
            alertDialog.setView(view)
                        .setTitle(getString(R.string.create_dialog_title))
                        .setPositiveButton(getString(R.string.create_dialog_positive)) { _: DialogInterface, _: Int -> }
                        .setNegativeButton(getString(R.string.create_dialog_negative)) { _: DialogInterface, _: Int -> }
                        .setCancelable(true)
            val dialog = alertDialog.create()
            dialog.show()
            editText.addTextChangedListener(
                    object : TextWatcher {
                        override fun afterTextChanged(text: Editable?) {
                            if (editText.text.isNotEmpty() && isError)
                                textInputLayout.error = null
                        }
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
                    }
            )
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val text = editText.text.toString()
                if (text.isEmpty()) {
                    isError = true
                    textInputLayout.error = getString(R.string.create_dialog_error)
                } else {
                    DataUtils.addKey(applicationContext, text)
                    DataUtils.modifyMainKey(applicationContext, text)
                    if (notifiedGroup == null) {
                        DataUtils.modifyNotificationKey(applicationContext, text)
                        Alarm.schedule(this)
                    }
                    (application as ScheduleApp).createNewGroup(text)
                    dialog.dismiss()
                    finish()
                }
            }
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
        binding.searchView.requestFocus()
    }

    override fun onPause() {
        super.onPause()
        DataUtils.hideKeyboard(this)
    }

    private class SearchTask(private val text: String,
                             private val keys: List<String>,
                             private val adapter: ArrayAdapter<String>) :
            AsyncTask<Void, Void, ArrayList<String>>() {

        companion object {
            private const val SEARCH_LIMIT = 15
        }

        override fun doInBackground(vararg p0: Void?): ArrayList<String> {
            val result = ArrayList<String>()
            FuzzySearch.extractTop(text, keys, SEARCH_LIMIT).forEach { result.add(it.string) }
            return result
        }

        override fun onPostExecute(result: ArrayList<String>) {
            adapter.clear()
            adapter.addAll(result)
            super.onPostExecute(result)
        }
    }
}