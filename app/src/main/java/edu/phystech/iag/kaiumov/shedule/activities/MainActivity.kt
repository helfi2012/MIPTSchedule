package edu.phystech.iag.kaiumov.shedule.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.preference.PreferenceManager
import edu.phystech.iag.kaiumov.shedule.*
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils
import edu.phystech.iag.kaiumov.shedule.notification.Alarm
import edu.phystech.iag.kaiumov.shedule.timetable.DaysPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.FullscreenPromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal


class MainActivity : AppCompatActivity() {

    var page = TimeUtils.getCurrentDay() - 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setNightSky()
        supportActionBar!!.title = ""
        pager.offscreenPageLimit = 7

        // New lesson button listener
        createButton.setOnClickListener {
            page = pager.currentItem
            val intent = Intent(applicationContext, EditActivity::class.java)
            intent.action = Keys.ACTION_NEW
            intent.putExtra(Keys.DAY, pager.currentItem + 1)
            startActivity(intent)
//            val item = ScheduleItem("Мат. Анализ", "Иванов Г.Е", "239 НК", 0,
//                    "LEC", "12:15", "14:00", "")
//            Notificator.showNotification(this, item)
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
        page = intent.getIntExtra(Keys.PAGE, TimeUtils.getCurrentDay() - 1)
        createTimeTable()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        page = pager.currentItem
        when (id) {
            R.id.delete_group -> {
                val keys = DataUtils.loadKeys(applicationContext)!!.toMutableList()
                val keyToRemove = keys[groupSpinner.selectedItemPosition]
                keys.remove(keyToRemove)
                DataUtils.modifyKeys(applicationContext, keys)
                Alarm.schedule(this)
                createTimeTable()
            }
            R.id.settings_button -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
            }
        }
        return true
    }

    private fun setNightSky() {
        if (ThemeHelper.isDark(this)) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val nightSky = preferences.getBoolean(getString(R.string.pref_stars_key), true)
            if (nightSky) {
                starsWhite.visibility = View.VISIBLE
                starsWhite.onStart()
            } else {
                starsWhite.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * This view is needed to show intro tip. It is set from GroupFragment
     */
    var listItemView: View? = null
    /**
     * Show intro tips only once. This function is called from GroupFragment
     */
    fun showTips() {
        // Check if tips were already shown
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val key = getString(R.string.pref_tip_key)
        val shown = preferences.getBoolean(key, false)
        if (shown)
            return
        val editor = preferences.edit()
        editor.putBoolean(key, true)
        editor.apply()

        // Show tips sequence
        MaterialTapTargetSequence().addPrompt(MaterialTapTargetPrompt.Builder(this)
                        .setTarget(createButton)
                        .setPrimaryText(R.string.tip_add_subject_title)
                        .setSecondaryText(R.string.tip_add_subject_summary)
                        .setAnimationInterpolator(LinearOutSlowInInterpolator())
                        .setFocalPadding(R.dimen.focal_padding)
                        .setCaptureTouchEventOutsidePrompt(true)
                        .setCaptureTouchEventOnFocal(true)
                        .create())
                .addPrompt(MaterialTapTargetPrompt.Builder(this)
                        .setTarget(groupSpinner)
                        .setPrimaryText(getString(R.string.tip_add_group_title))
                        .setSecondaryText(getString(R.string.tip_add_group_summary))
                        .setIcon(R.drawable.ic_add)
                        .setFocalPadding(R.dimen.focal_padding)
                        .setCaptureTouchEventOutsidePrompt(true)
                        .setCaptureTouchEventOnFocal(true)
                        .create())
                .addPrompt(MaterialTapTargetPrompt.Builder(this)
                        .setTarget(listItemView)
                        .setPrimaryText(R.string.tip_edit_title)
                        .setSecondaryText(R.string.tip_edit_summary)
                        .setPromptBackground(FullscreenPromptBackground())
                        .setPromptFocal(RectanglePromptFocal())
                        .setCaptureTouchEventOutsidePrompt(true)
                        .setCaptureTouchEventOnFocal(true)
                        .create())
                .addPrompt(MaterialTapTargetPrompt.Builder(this)
                        .setTarget(toolbar.getChildAt(1))
                        .setPrimaryText(getString(R.string.tip_settings_title))
                        .setSecondaryText(getString(R.string.tip_settings_summary))
                        .setAnimationInterpolator(LinearOutSlowInInterpolator())
                        .setFocalPadding(R.dimen.focal_padding)
                        .setIcon(R.drawable.ic_more_vert)
                        .setCaptureTouchEventOutsidePrompt(true)
                        .setCaptureTouchEventOnFocal(true)
                        .create())
                .show()
    }

    private fun createTimeTable() {
        val keys = DataUtils.loadKeys(applicationContext)

        val app = application as ScheduleApp
        val timetable = app.timetable
        if (keys == null) {
            // Start selection activity to select group
            startActivity(Intent(this, StartActivity::class.java))
        } else {
            val mainKey = DataUtils.loadMainKey(applicationContext)
            setUpSpinner(keys, keys.indexOf(mainKey))
            if (timetable.isNotEmpty()) {
                // Creating schedule view
                pager.adapter = DaysPagerAdapter(timetable[mainKey] ?: ArrayList(),
                        applicationContext,
                        supportFragmentManager)
                tabs.setupWithViewPager(pager)
                pager.currentItem = page
            }
        }
    }

    private fun setUpSpinner(keys: List<String>, selection: Int) {
        val adapter = ArrayAdapter<String>(this, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        adapter.addAll(keys)
        adapter.add(getString(R.string.add_group))
        groupSpinner.adapter = adapter
        groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == keys.size) {
                    startActivity(Intent(applicationContext, StartActivity::class.java))
                } else if (position != selection) {
                    page = pager.currentItem
                    DataUtils.modifyMainKey(applicationContext, adapter.getItem(position)!!)
                    createTimeTable()
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        groupSpinner.setSelection(selection)
    }
}