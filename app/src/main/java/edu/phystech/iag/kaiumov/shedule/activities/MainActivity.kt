package edu.phystech.iag.kaiumov.shedule.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import edu.phystech.iag.kaiumov.shedule.*
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private var day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2

    /**
     * Creates alert dialog to ask user whether to reset schedule or not.
     */
    private fun showConfirmDialog() {
        val app = application as ScheduleApp
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(getString(R.string.dialog_title))
        alertDialog.setMessage(getString(R.string.reset_message))
        alertDialog.setPositiveButton(getString(R.string.button_yes)) { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.cancel()
            app.resetSchedule(this) { createTimeTable() }
        }
        alertDialog.setNegativeButton(getString(R.string.button_no)) { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.cancel()
        }
        alertDialog.setCancelable(true)
        alertDialog.create().show()
    }

    /**
     * This function is called after schedule is created from Classes Adapter.
     *
     * @param listTip is first tip that shows schedule item
     */
    internal fun showTips(listTip: MaterialShowcaseView) {
        val createTip = MaterialShowcaseView.Builder(this)
                .setTarget(createButton)
                .setDismissText(getString(R.string.confirm_tip))
                .setContentText(getString(R.string.fab_tip))
                .singleUse("1")
                .setDismissOnTouch(true)
                .build()
        val config = ShowcaseConfig()
        config.delay = 200
        val sequence = MaterialShowcaseSequence(this, "2")
        sequence.setConfig(config)
        sequence.addSequenceItem(listTip)
        sequence.addSequenceItem(createTip)
        sequence.setOnItemDismissedListener { _, i ->
            if (i == 1) {
                day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
                day = if (day in 0..6) day else 6
                pager.setCurrentItem(day, true)
            }
        }
        sequence.start()
    }

    /**
     * Creating pager view with recycler view fragments
     */
    private fun createTimeTable() {
        val key: String? = Utils.loadKey(applicationContext)
        val app = application as ScheduleApp
        val timetable = app.timetable ?: return
        if (key == null || !timetable.containsKey(key)) {
            // Start selection activity to select group
            startActivity(Intent(this, StartActivity::class.java))
        } else
        if (timetable.isNotEmpty()) {
            // Creating schedule view
            supportActionBar!!.title = key
            pager.adapter = DaysPagerAdapter(timetable[key] ?: ArrayList(),
                    applicationContext,
                    supportFragmentManager)
            tabs.setupWithViewPager(pager)
            day = if (day in 0..6) day else 6
            pager.currentItem = day
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        // Spaces switcher initializing
        menu.findItem(R.id.spaces_switcher).isChecked =
                PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(Keys.PREF_SPACES, false)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        day = pager.currentItem
        when (id) {
            R.id.change_group -> startActivity(Intent(this, StartActivity::class.java))
            R.id.reset_schedule -> showConfirmDialog()
            R.id.spaces_switcher -> {
                // turn on/off spaces between lessons
                val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                item.isChecked = !preferences.getBoolean(Keys.PREF_SPACES, false)
                val editor = preferences.edit()
                editor.putBoolean(Keys.PREF_SPACES, item.isChecked)
                editor.apply()
                createTimeTable()
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        pager.offscreenPageLimit = 7
        // New lesson button listener
        createButton.setOnClickListener {
            val intent = Intent(applicationContext, EditActivity::class.java)
            intent.action = Keys.ACTION_NEW
            intent.putExtra(Keys.DAY, pager.currentItem + 1)
            startActivity(intent)
        }
    }

    override fun onPause() {
        // Save current day
        intent.putExtra(Keys.DAY, pager.currentItem)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // Load current day and create schedule
        day = intent.getIntExtra(Keys.DAY, day)
        createTimeTable()
    }
}