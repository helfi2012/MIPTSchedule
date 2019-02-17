package edu.phystech.iag.kaiumov.shedule

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem

class DaysPagerAdapter(private val timetable: HashMap<String, ArrayList<ScheduleItem>>,
                       private val keys: List<String>,
                       fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return DayFragment.new(keys[position], timetable[keys[position]]!!)
    }

    override fun getCount(): Int = keys.size

    override fun getPageTitle(position: Int): CharSequence {
        return keys[position]
    }
}