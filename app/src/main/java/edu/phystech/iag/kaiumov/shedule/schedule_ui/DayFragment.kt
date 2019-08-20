package edu.phystech.iag.kaiumov.shedule.schedule_ui

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.activities.MainActivity
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils
import kotlinx.android.synthetic.main.fragment_group.*
import java.util.*


class DayFragment : Fragment() {

    private var lastIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val key = arguments?.getString(ARG_KEY)
        val sData = arguments?.getSerializable(ARG_DATA)
        if (key == null || sData == null)
            return
        var data = sData as List<ScheduleItem>
        val adapter = RecyclerAdapter(activity!!, key)
        // Add data to recycler view
        val days = recycler.context.resources.getStringArray(R.array.week)
        val daysIndex = TreeSet<Int>()
        data.forEach { daysIndex.add(it.day) }
        // Sort data
        data = data.sortedBy { TimeUtils.getCalendarTime(it.day, it.startTime) }
        // Add next item to tag
        (0 until data.size - 1).forEach {
            if (data[it].day == data[it + 1].day)
                data[it].tag = data[it + 1]
            else
                data[it].tag = null
        }
        data[data.size - 1].tag = null
        for (day in daysIndex.iterator()) {
            adapter.setHeaderAndData(data.filter { it.day == day },
                    HeaderDataImpl(day, days[day - 1], R.layout.recycler_header))
        }
        // Set recycler adapter
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = adapter
        val day = TimeUtils.getCurrentDay()
        var index = data.indexOfFirst { it.day == day }
        if (index == -1) {
            index = 0
        } else {
            index += day - 1
        }
        recycler.scrollToPosition(index)

        if (adapter.getItemViewType(index) == HeaderDataImpl.HEADER) {
            lastIndex = index + 1
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onResume() {
        super.onResume()
        Handler().post {
            val mainActivity = activity as MainActivity
            mainActivity.listItemView = recycler.layoutManager?.findViewByPosition(lastIndex)
            mainActivity.showTips()
        }
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