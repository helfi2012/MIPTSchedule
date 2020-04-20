package edu.phystech.iag.kaiumov.shedule.timetable

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem

class DaysPagerAdapter(private val timetable: ArrayList<ScheduleItem>, private val context: Context,
                       fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return DayFragment.new(position + 1, timetable)
    }

    override fun getCount(): Int = 7

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getStringArray(R.array.week_short)[position].toString()
    }
}