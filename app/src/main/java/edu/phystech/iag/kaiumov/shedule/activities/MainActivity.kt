package edu.phystech.iag.kaiumov.shedule.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import edu.phystech.iag.kaiumov.shedule.*
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig


class MainActivity : AppCompatActivity() {

    private var page = Keys.DEFAULT_PAGE
    private var keys: List<String>? = null

    internal var listItemView: View? = null
    private var addButtonView: View? = null

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
     * Show intro tips after schedule and menu options are created.
     */
    private fun showTips() {
        val config = ShowcaseConfig()
        config.delay = 200
        val sequence = MaterialShowcaseSequence(this, "3")
        sequence.setConfig(config)
        if (addButtonView != null)
            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(addButtonView)
                    .setDismissText(getString(R.string.confirm_tip))
                    .setContentText(getString(R.string.add_group_tip))
                    .setTargetTouchable(false)
                    .singleUse("0")
                    .setDismissOnTouch(true)
                    .build())
        sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                .setTarget(createButton)
                .setDismissText(getString(R.string.confirm_tip))
                .setContentText(getString(R.string.fab_tip))
                .setTargetTouchable(false)
                .singleUse("1")
                .setDismissOnTouch(true)
                .build())
        if (listItemView != null)
            sequence.addSequenceItem(MaterialShowcaseView.Builder(this)
                    .setTarget(listItemView)
                    .setDismissText(getString(R.string.confirm_tip))
                    .setContentText(getString(R.string.list_tip))
                    .withRectangleShape()
                    .setTargetTouchable(false)
                    .singleUse("2")
                    .setDismissOnTouch(true)
                    .build())
        sequence.start()
    }

    /**
     * Creating pager view with recycler view fragments
     */
    private fun createTimeTable() {
        keys = Utils.loadKeys(applicationContext)
        val app = application as ScheduleApp
        val timetable = app.timetable ?: return
        if (keys == null) {
            // Start selection activity to select group
            startActivity(Intent(this, StartActivity::class.java))
        } else
        if (timetable.isNotEmpty()) {
            // Creating schedule view
            pager.adapter = DaysPagerAdapter(timetable, keys!!, supportFragmentManager)
            tabs.setupWithViewPager(pager)
            pager.currentItem = page
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        // Spaces switcher initializing
        menu.findItem(R.id.spaces_switcher).isChecked =
                PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean(Keys.PREF_SPACES, false)
        Handler().post {
            addButtonView = findViewById(R.id.add_group)
            showTips()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        page = pager.currentItem
        when (id) {
            R.id.add_group -> startActivity(Intent(this, StartActivity::class.java))
            R.id.reset_schedule -> showConfirmDialog()
            R.id.delete_group -> {
                val keys = Utils.loadKeys(applicationContext)!!.toMutableList()
                val keyToRemove = keys[pager.currentItem]
                keys.remove(keyToRemove)
                Utils.modifyKeys(applicationContext, keys)
                createTimeTable()
            }
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
        supportActionBar!!.title = resources.getString(R.string.app_name)
        pager.offscreenPageLimit = 7
        // New lesson button listener
        createButton.setOnClickListener {
            page = pager.currentItem
            val intent = Intent(applicationContext, EditActivity::class.java)
            intent.action = Keys.ACTION_NEW
            intent.putExtra(Keys.KEY, keys!![page])
            startActivity(intent)
        }
    }

    override fun onPause() {
        // Save current day
        intent.putExtra(Keys.PAGE, pager.currentItem)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // Load current day and create schedule
        page = intent.getIntExtra(Keys.PAGE, Keys.DEFAULT_PAGE)
        createTimeTable()
    }
}