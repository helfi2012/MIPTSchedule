package edu.phystech.iag.kaiumov.shedule.activities

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.ScheduleApp
import edu.phystech.iag.kaiumov.shedule.ThemeHelper
import edu.phystech.iag.kaiumov.shedule.notification.Alarm


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (supportFragmentManager.findFragmentById(android.R.id.content) == null) {
            supportFragmentManager.beginTransaction().add(android.R.id.content, SettingsFragment()).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_screen)

            findPreference<Preference>(getString(R.string.pref_reset_key))?.setOnPreferenceClickListener {
                showConfirmDialog()
                true
            }

            val themePreference = findPreference<ListPreference>(getString(R.string.pref_theme_key))
            themePreference?.setOnPreferenceChangeListener { _, newValue ->
                ThemeHelper.applyTheme(newValue as String)
                true
            }

            findPreference<ListPreference>(getString(R.string.pref_notification_before_key))?.setOnPreferenceChangeListener { _, _ ->
                Alarm.schedule(context!!)
                true
            }

            findPreference<Preference>(getString(R.string.pref_notification_system_key))?.setOnPreferenceClickListener {
                val intent = Intent()
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context?.packageName)
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                        intent.putExtra("app_package", context?.packageName)
                        intent.putExtra("app_uid", context?.applicationInfo?.uid)
                    }
                    else -> {
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.addCategory(Intent.CATEGORY_DEFAULT)
                        intent.data = Uri.parse("package:" + context?.packageName)
                    }
                }
                startActivity(intent)
                true
            }

            for (i in 0 until preferenceScreen.preferenceCount) {
                val pref = preferenceScreen.getPreference(i) as PreferenceCategory
                pref.isIconSpaceReserved = false
                for (j in 0 until pref.preferenceCount) {
                    pref.getPreference(j).isIconSpaceReserved = false
                }
            }

        }

        /**
         * Creates alert dialog to ask user whether to reset schedule or not.
         */
        private fun showConfirmDialog() {
            val app = activity?.application as ScheduleApp
            val alertDialog = AlertDialog.Builder(activity!!)
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
}
