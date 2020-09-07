package edu.phystech.iag.kaiumov.shedule.timetable

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.activities.MainActivity
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.utils.TimeUtils
import kotlinx.android.synthetic.main.fragment_day.*
import java.lang.Exception
import java.util.*


class DayFragment : Fragment() {

    private val timeInterval = 30 * 1000L
    private val timer = Timer()
    private var empty = false
    private var day: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        day = arguments?.getInt(ARG_DAY) ?: 0
        val timetable = arguments?.getSerializable(ARG_TIMETABLE) ?: return null
        val lessons = timetable as List<*>
        if (lessons.none { (it as ScheduleItem).day == day }) {
            empty = true
            return when (day) {
                7 -> inflater.inflate(R.layout.empty_day, container, false)
                else -> null
            }
        }
        return inflater.inflate(R.layout.fragment_day, container, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val day = arguments?.getInt(ARG_DAY)
        val timetable = arguments?.getSerializable(ARG_TIMETABLE)
        if (day == null || timetable == null || empty)
            return

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val showSpaces = preferences.getBoolean(getString(R.string.pref_show_spaces), false)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = ClassesAdapter(day, showSpaces, timetable as List<ScheduleItem>)

        if (day == TimeUtils.getCurrentDay()) {
            timer.schedule(object : TimerTask() {
                override fun run() {
                    try {
                        activity?.runOnUiThread {
                            recycler.adapter?.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {}
                }
            }, 0L, timeInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as MainActivity
        if (day - 1 == mainActivity.page && shouldShowTips()) {
            Handler().post {
                if (!empty) {
                    mainActivity.listItemView = recycler.layoutManager?.findViewByPosition(0)
                }
                mainActivity.showTips()
            }
        }
    }

    override fun onDestroy() {
        // Free memory
        if (!empty) {
            view?.findViewById<ImageView>(R.id.imageView)?.setImageBitmap(null)
        }
        timer.cancel()
        super.onDestroy()
    }

    private fun shouldShowTips() : Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val key = resources.getString(R.string.pref_tip_key)
        return !preferences.getBoolean(key, false)
    }

    companion object {
        private const val ARG_DAY = "day"
        private const val ARG_TIMETABLE = "source_old"

        fun new(day: Int, timetable: ArrayList<ScheduleItem>): DayFragment {
            val fragment = DayFragment()
            val arguments = Bundle()
            arguments.putInt(ARG_DAY, day)
            arguments.putSerializable(ARG_TIMETABLE, timetable)
            fragment.arguments = arguments
            return fragment
        }
    }
}