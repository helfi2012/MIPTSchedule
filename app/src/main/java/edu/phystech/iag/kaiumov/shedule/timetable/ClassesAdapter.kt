package edu.phystech.iag.kaiumov.shedule.timetable

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import edu.phystech.iag.kaiumov.shedule.ColorUtil
import edu.phystech.iag.kaiumov.shedule.Keys
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.activities.EditActivity
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils
import kotlinx.android.synthetic.main.schedule_item.view.*


class ClassesAdapter(private val day: Int, private val showSpaces: Boolean, classes: List<ScheduleItem>) :
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
        // Set time layout background
        holder.view.time_layout.background = ContextCompat.getDrawable(context, ColorUtil.getBackgroundDrawable(item.type))
        // Set text fields
        holder.view.name.text = item.name
        holder.view.prof.text = item.prof
        holder.view.place.text = item.place
        holder.view.startTime.text = item.startTime
        holder.view.endTime.text = item.endTime
        // Set time field text color
        val textColor = ContextCompat.getColor(context, ColorUtil.getTextColor(item.type))
        holder.view.startTime.setTextColor(textColor)
        holder.view.endTime.setTextColor(textColor)
        holder.view.timeLine.setBackgroundColor(textColor)
        holder.view.dot.setColorFilter(textColor)
        // Dot and selection layout
        holder.view.dot.visibility = View.INVISIBLE
        holder.view.selectionLayout.background = null

        // Increase item height for longer classes
        var scale = maxOf(item.length() / (1 + 25.0 / 60.0), 1.0)
        holder.view.scheduleMainLayout.layoutParams.height = (holder.view.context.resources.getDimension(R.dimen.schedule_item_height) * scale).toInt()
        holder.view.scheduleMainLayout.setOnClickListener {
            val intent = Intent(context, EditActivity::class.java)
            intent.action = Keys.ACTION_EDIT
            intent.putExtra(Keys.ITEM, item)
            context.startActivity(intent)
        }
        // Spaces
        if (showSpaces && position + 1 < data.size) {
//            val length = maxOf(TimeUtils.length(item.endTime, data[position + 1].startTime) - 1.0 / 6.0, 0.0)
//            val marginParams = holder.view.rootLayout.layoutParams as ViewGroup.MarginLayoutParams
//            marginParams.bottomMargin = (holder.view.context.resources.getDimension(R.dimen.schedule_item_height) * length).toInt()
            val length = maxOf(TimeUtils.length(item.endTime, data[position + 1].startTime) * 60, 0.0).toInt()
            holder.view.breakTextView.text = when (length) {
                in 0..10 -> context.resources.getString(R.string.break_extra_short, length)
                in 11..20 -> context.resources.getString(R.string.break_short, length)
                in 21..40 -> context.resources.getString(R.string.break_medium, length)
                else -> context.resources.getString(R.string.break_long, length)
            }
            val breakDrawable = when (length) {
                in 0..10 -> R.drawable.ic_run_24px
                in 11..20 -> R.drawable.ic_local_cafe_24px
                in 21..40 -> R.drawable.ic_restaurant_24px
                else -> R.drawable.ic_hotel_24px
            }
            holder.view.breakTextView.setCompoundDrawablesWithIntrinsicBounds(breakDrawable, 0, 0, 0)
            holder.view.breakTextView.visibility = View.VISIBLE
        }
        // Set up actual item
        val day = TimeUtils.getCurrentDay()
        val time = TimeUtils.getCurrentTime()
        if (day == item.day && TimeUtils.compareTime(time, item.startTime) >= 0 &&
                TimeUtils.compareTime(time, item.endTime) <= 0) {
            // Change font
            holder.view.place.typeface = ResourcesCompat.getFont(context, R.font.futura_demi)
            // Set selection color
            holder.view.selectionLayout.setBackgroundColor(ContextCompat.getColor(context, ColorUtil.getBackgroundColor(item.type)))
            holder.view.selectionLayout.alpha = context.resources.getFloat(R.dimen.selection_alpha)
            // Move dot
            scale = TimeUtils.length(item.startTime, time) / item.length()
            val constraintSet = ConstraintSet()
            holder.view.dot.visibility = View.VISIBLE
            constraintSet.clone(holder.view.constraintLayout)
            constraintSet.setVerticalBias(R.id.dot, scale.toFloat())
            constraintSet.applyTo(holder.view.constraintLayout)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = position.toLong()

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}