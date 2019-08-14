package edu.phystech.iag.kaiumov.shedule.activities

import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.ScheduleApp
import android.provider.Settings
import edu.phystech.iag.kaiumov.shedule.notification.Alarm


/**
 * A [PreferenceActivity] that presents a set of application settings.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val nightMode = preferences.getBoolean(getString(R.string.pref_night_key), false)
        if (nightMode) {
            setTheme(R.style.AppTheme_Dark)
        }
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (fragmentManager.findFragmentById(android.R.id.content) == null) {
            fragmentManager.beginTransaction()
                    .add(android.R.id.content, SettingsFragment()).commit()
        }
    }

    override fun onBackPressed() {
        Alarm.schedule(this)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_screen)
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_notification_ringtone_key)))
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_notification_before_key)))

            findPreference(getString(R.string.pref_reset_key)).setOnPreferenceClickListener {
                showConfirmDialog()
                true
            }
            
            findPreference(getString(R.string.pref_night_key)).setOnPreferenceChangeListener { _, _ ->
                activity.recreate()
                true
            }

            val systemNotification = findPreference(getString(R.string.pref_notification_system_key))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                systemNotification.setOnPreferenceClickListener {
                    val intent = Intent()
                    when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                            intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                            intent.putExtra("app_package", context.packageName)
                            intent.putExtra("app_uid", context.applicationInfo.uid)
                        }
                        else -> {
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                            intent.data = Uri.parse("package:" + context.packageName)
                        }
                    }
                    context.startActivity(intent)
                    true
                }
            }
        }

        /**
         * Creates alert dialog to ask user whether to reset schedule or not.
         */
        private fun showConfirmDialog() {
            val app = activity.application as ScheduleApp
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle(getString(R.string.dialog_title))
            alertDialog.setMessage(getString(R.string.reset_message))
            alertDialog.setPositiveButton(getString(R.string.button_yes)) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.cancel()
                app.resetSchedule()
            }
            alertDialog.setNegativeButton(getString(R.string.button_no)) { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.cancel()
            }
            alertDialog.setCancelable(true)
            alertDialog.create().show()
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val index = preference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            preference.entries[index]
                        else
                            null)

            } else if (preference is RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent)

                } else {
                    val ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue))

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null)
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }
}
