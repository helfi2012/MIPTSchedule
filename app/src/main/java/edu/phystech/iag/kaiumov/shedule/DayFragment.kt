package edu.phystech.iag.kaiumov.shedule

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils
import edu.phystech.iag.kaiumov.shedule.recyclerview.HeaderDataImpl
import edu.phystech.iag.kaiumov.shedule.recyclerview.RecyclerAdapter
import kotlinx.android.synthetic.main.fragment_day.*
import java.util.*
import kotlin.collections.HashSet


class DayFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_day, container, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val key = arguments?.getString(ARG_KEY)
        val sData = arguments?.getSerializable(ARG_DATA)
        if (key == null || sData == null)
            return
        var data = sData as List<ScheduleItem>
        recycler.layoutManager = LinearLayoutManager(context)
        val adapter = RecyclerAdapter(activity!!, key)
        // Add data to recycler view
        val days = recycler.context.resources.getStringArray(R.array.week)
        val daysIndex = HashSet<Int>()
        data.forEach { daysIndex.add(it.day) }
        // Sort data
        data = data.sortedWith(Comparator { t1, t2 ->
            return@Comparator if (t1.day != t2.day)
                t1.day - t2.day
            else
                TimeUtils.compareTime(t1.startTime, t2.startTime)
        })
        // Add next item to tag
        (0 until data.size - 1).forEach {
            if (data[it].day == data[it + 1].day)
                data[it].tag = data[it + 1]
            else
                data[it].tag = null }
        data[data.size - 1].tag = null
        for (day in daysIndex.iterator()) {
            adapter.setHeaderAndData(data.filter { it.day == day }, HeaderDataImpl(day, days[day - 1].toString(),
                    R.layout.recycler_header))
        }
        // Set recycler adapter
        recycler.adapter = adapter
        val day = TimeUtils.getCurrentDay()
        val index = data.indexOfFirst { it.day == day } + day - 1
        recycler.scrollToPosition(index)
    }

    companion object {
        private const val ARG_KEY = "key"
        private const val ARG_DATA = "source"

        fun new(key: String, data: ArrayList<ScheduleItem>): DayFragment {
            val fragment = DayFragment()
            val arguments = Bundle()
            arguments.putString(ARG_KEY, key)
            arguments.putSerializable(ARG_DATA, data)
            fragment.arguments = arguments
            return fragment
        }
    }
}