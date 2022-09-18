package edu.phystech.iag.kaiumov.schedule.ui.timetable

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import edu.phystech.iag.kaiumov.schedule.Keys
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.databinding.ScheduleItemBinding
import edu.phystech.iag.kaiumov.schedule.ui.activities.EditActivity
import edu.phystech.iag.kaiumov.schedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.schedule.utils.ColorUtil
import edu.phystech.iag.kaiumov.schedule.utils.TimeUtils


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

    inline fun View.afterMeasured(crossinline f: View.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = position.toLong()

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ScheduleItemBinding.bind(view)

        fun bind(item: ScheduleItem) {
            with(binding) {
                // Set time layout background
                timeLayout.background = ContextCompat.getDrawable(view.context, ColorUtil.getBackgroundDrawable(item.type))
                // Set text fields
                name.text = item.name
                prof.text = item.prof
                place.text = item.place
                startTime.text = item.startTime
                endTime.text = item.endTime
                // Set time field text color
                val textColor = ContextCompat.getColor(view.context, ColorUtil.getTextColor(item.type))
                startTime.setTextColor(textColor)
                endTime.setTextColor(textColor)
                timeLine.setBackgroundColor(textColor)
                dot.setColorFilter(textColor)
                // Dot and selection layout
                dot.visibility = View.INVISIBLE
                selectionLayout.background = null

                // Increase item height for longer classes
                // holder.view.scheduleMainLayout.layoutParams.height = (holder.view.context.resources.getDimension(R.dimen.schedule_item_height) * scale).toInt()
                val scale = maxOf(item.length() / (1 + 25.0 / 60.0), 1.0)
                val defaultHeight = view.context.resources.getDimension(R.dimen.schedule_item_height)
                scheduleMainLayout.measure(0, 0)
                scheduleMainLayout.layoutParams.height = maxOf(
                    (defaultHeight * scale).toInt(),
                    scheduleMainLayout.measuredHeight
                )

                scheduleMainLayout.setOnClickListener {
                    val intent = Intent(view.context, EditActivity::class.java)
                    intent.action = Keys.ACTION_EDIT
                    intent.putExtra(Keys.ITEM, item)
                    view.context.startActivity(intent)
                }
                // Spaces
                if (showSpaces && position + 1 < data.size) {
                    val length = maxOf(TimeUtils.length(item.endTime, data[position + 1].startTime) * 60, 0.0).toInt()
                    breakTextView.text = breakText(view.context, length)
                    breakTextView.setCompoundDrawablesWithIntrinsicBounds(breakDrawable(length), 0, 0, 0)
                    breakTextView.visibility = View.VISIBLE
                }
                // Set up actual item
                val day = TimeUtils.getCurrentDay()
                val time = TimeUtils.getCurrentTime()
                if (day == item.day && TimeUtils.compareTime(time, item.startTime) >= 0 &&
                    TimeUtils.compareTime(time, item.endTime) <= 0) {
                    // Change font
                    place.typeface = ResourcesCompat.getFont(view.context, R.font.futura_demi)
                    // Set selection color
                    selectionLayout.setBackgroundColor(ContextCompat.getColor(view.context, ColorUtil.getBackgroundColor(item.type)))

                    val typedValue = TypedValue()
                    view.context.resources.getValue(R.dimen.selection_alpha, typedValue, true)
                    selectionLayout.alpha = typedValue.float
                    // Move dot
                    val dotPosition = TimeUtils.length(item.startTime, time) / item.length()
                    val constraintSet = ConstraintSet()
                    dot.visibility = View.VISIBLE
                    constraintSet.clone(constraintLayout)
                    constraintSet.setVerticalBias(R.id.dot, dotPosition.toFloat())
                    constraintSet.applyTo(constraintLayout)
                }
            }
        }
    }

    private fun breakText(context: Context, length: Int): String {
        return when (length) {
            in 0..10 -> context.resources.getString(R.string.break_extra_short, length)
            in 11..20 -> context.resources.getString(R.string.break_short, length)
            in 21..40 -> context.resources.getString(R.string.break_medium, length)
            else -> context.resources.getString(R.string.break_long, length)
        }
    }

    private fun breakDrawable(length: Int): Int {
        return when (length) {
            in 0..10 -> R.drawable.ic_run_24px
            in 11..20 -> R.drawable.ic_local_cafe_24px
            in 21..40 -> R.drawable.ic_restaurant_24px
            else -> R.drawable.ic_hotel_24px
        }
    }
}