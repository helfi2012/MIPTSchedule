package edu.phystech.iag.kaiumov.shedule.timetable

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.phystech.iag.kaiumov.shedule.Keys
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.activities.EditActivity
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import kotlinx.android.synthetic.main.schedule_item.view.*


class ClassesAdapter(private val day: Int, classes: List<ScheduleItem>) :
        RecyclerView.Adapter<ClassesAdapter.ViewHolder>() {
    /**
     * Filtered list with only the classes for the given day
     */
    private val data: List<ScheduleItem> = classes.filter { it.day == day }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                as LayoutInflater
        val v = layoutInflater.inflate(R.layout.schedule_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Set item view
        val item = data[position]
        val context = holder.view.context
        holder.view.time_layout.background = ContextCompat.getDrawable(context, lessonDrawable(item.type))
        holder.view.name.text = item.name
        holder.view.prof.text = item.prof
        holder.view.place.text = item.place
        holder.view.startTime.text = item.startTime
        holder.view.endTime.text = item.endTime

        // Increase item height for longer classes
        val scale = maxOf(item.length(), 1.0)
        holder.view.scheduleMainLayout.layoutParams.height = (holder.view.context.resources.getDimension(R.dimen.schedule_item_height) * scale).toInt()
        holder.view.scheduleMainLayout.setOnClickListener {
            val intent = Intent(context, EditActivity::class.java)
            intent.action = Keys.ACTION_EDIT
            intent.putExtra(Keys.ITEM, item)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = position.toLong()

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    companion object {
        internal fun lessonDrawable(type: String): Int {
            return when (type) {
                "LAB" -> R.drawable.bg_item_lab
                "SEM" -> R.drawable.bg_item_sem
                else -> R.drawable.bg_item_lec
            }
        }
    }
}