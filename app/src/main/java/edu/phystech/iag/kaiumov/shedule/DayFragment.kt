package edu.phystech.iag.kaiumov.shedule

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import kotlinx.android.synthetic.main.fragment_day.*
import java.util.*


class DayFragment : Fragment() {

    private var empty = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val day = arguments?.getInt(ARG_DAY)
        val timetable = arguments?.getSerializable(ARG_TIMETABLE)
        if (day == null || timetable == null)
            return null
        val lessons = timetable as List<*>
        if (lessons.none { (it as ScheduleItem).day == day }) {
            empty = true
            return when (day) {
                7 -> inflater.inflate(R.layout.empty1, container, false)
                else -> inflater.inflate(R.layout.empty2, container, false)
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
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = ClassesAdapter(day, activity!!, timetable as List<ScheduleItem>)
    }

    override fun onDestroy() {
        // Free memory
        if (!empty) {
            view?.findViewById<ImageView>(R.id.imageView)?.setImageBitmap(null)
        }
        super.onDestroy()
    }

    companion object {
        private const val ARG_DAY = "day"
        private const val ARG_TIMETABLE = "source"

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