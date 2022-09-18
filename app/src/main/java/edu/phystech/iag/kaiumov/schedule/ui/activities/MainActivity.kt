package edu.phystech.iag.kaiumov.schedule.ui.activities


import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.preference.PreferenceManager
import edu.phystech.iag.kaiumov.schedule.*
import edu.phystech.iag.kaiumov.shedule.databinding.ActivityMainBinding
import edu.phystech.iag.kaiumov.schedule.utils.TimeUtils
import edu.phystech.iag.kaiumov.schedule.notification.Alarm
import edu.phystech.iag.kaiumov.schedule.ui.timetable.DaysPagerAdapter
import edu.phystech.iag.kaiumov.schedule.ui.helpers.CircularViewPagerHandler
import edu.phystech.iag.kaiumov.schedule.utils.DataUtils
import edu.phystech.iag.kaiumov.schedule.utils.ThemeHelper
import edu.phystech.iag.kaiumov.shedule.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.FullscreenPromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal


class MainActivity : AppCompatActivity() {

    var page = TimeUtils.getCurrentDay() - 1

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        setNightSky()
        supportActionBar!!.title = ""
        binding.pager.offscreenPageLimit = 7

        // New lesson button listener
        binding.createButton.setOnClickListener {
            page = binding.pager.currentItem
            val intent = Intent(this, EditActivity::class.java)
            intent.action = Keys.ACTION_NEW
            intent.putExtra(Keys.DAY, binding.pager.currentItem + 1)
            startActivity(intent)
        }
    }

    override fun onPause() {
        // Save current day
        intent.putExtra(Keys.PAGE, binding.pager.currentItem)
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
        page = binding.pager.currentItem
        when (id) {
            R.id.delete_group -> {
                val keys = DataUtils.loadKeys(applicationContext)!!.toMutableList()
                val keyToRemove = keys[binding.groupSpinner.selectedItemPosition]
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
            val preferences = PreferenceManager.getDefaultSharedPreferences(this)
            val nightSky = preferences.getBoolean(getString(R.string.pref_stars_key), true)
            if (nightSky) {
                binding.starsWhite.visibility = View.VISIBLE
                binding.starsWhite.onStart()
            } else {
                binding.starsWhite.visibility = View.INVISIBLE
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
                        .setTarget(binding.createButton)
                        .setPrimaryText(R.string.tip_add_subject_title)
                        .setSecondaryText(R.string.tip_add_subject_summary)
                        .setAnimationInterpolator(LinearOutSlowInInterpolator())
                        .setFocalPadding(R.dimen.focal_padding)
                        .setCaptureTouchEventOutsidePrompt(true)
                        .setCaptureTouchEventOnFocal(true)
                        .create())
                .addPrompt(MaterialTapTargetPrompt.Builder(this)
                        .setTarget(binding.groupSpinner)
                        .setPrimaryText(getString(R.string.tip_add_group_title))
                        .setSecondaryText(getString(R.string.tip_add_group_summary))
                        .setIcon(R.drawable.ic_add_inverted_24px)
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
                        .setTarget(binding.toolbar.getChildAt(1))
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
                binding.pager.adapter = DaysPagerAdapter(timetable[mainKey] ?: ArrayList(),
                        this,
                        supportFragmentManager)
                binding.tabs.setupWithViewPager(binding.pager)
                binding.pager.addOnPageChangeListener(CircularViewPagerHandler(binding.pager))
                binding.pager.currentItem = page
            }
        }
    }

    private fun setUpSpinner(entries: List<String>, selection: Int) {
        val adapter = object : ArrayAdapter<String>(this, R.layout.group_spinner_item) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v =  super.getDropDownView(position, convertView, parent)
                val tv = v.findViewById<TextView>(R.id.textView)
                tv.text = getItem(position)
                if (position == binding.groupSpinner.selectedItemPosition) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            R.drawable.ic_done_24px, 0)
                }
                if (position == count - 1) {
                    tv.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            R.drawable.ic_add_24px, 0)
                }
                return v
            }
        }
        adapter.setDropDownViewResource(R.layout.group_spinner_dropdown_item)
        adapter.addAll(entries)
        adapter.add(getString(R.string.add_group))
        binding.groupSpinner.adapter = adapter
        binding.groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == entries.size) {
                    startActivity(Intent(applicationContext, StartActivity::class.java))
                } else if (position != selection) {
                    page = binding.pager.currentItem
                    DataUtils.modifyMainKey(applicationContext, adapter.getItem(position)!!)
                    createTimeTable()
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.groupSpinner.setSelection(selection)
    }
}