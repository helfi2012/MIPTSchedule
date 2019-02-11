package edu.phystech.iag.kaiumov.shedule

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.phystech.iag.kaiumov.shedule.activities.EditActivity
import edu.phystech.iag.kaiumov.shedule.activities.MainActivity
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils
import kotlinx.android.synthetic.main.schedule_item.view.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView


class ClassesAdapter(private val day: Int, private val activity: Activity, classes: List<ScheduleItem>) :
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
        holder.view.scheduleMainLayout.layoutParams.height = (holder.view.context.resources.getDimension(R.dimen.schedule_item_height) * item.length()).toInt()
        holder.view.scheduleMainLayout.setOnClickListener {
            val intent = Intent(context, EditActivity::class.java)
            intent.action = Keys.ACTION_EDIT
            intent.putExtra(Keys.ITEM, item)
            context.startActivity(intent)
        }

        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val spaces = preferences.getBoolean(Keys.PREF_SPACES, false)
        val firstStart = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean(Keys.PREF_FIRST, true)
        // Create spaces
        if (spaces && position != data.size - 1) {
            holder.view.rootLayout.layoutParams.height = holder.view.scheduleMainLayout.height +
                    (TimeUtils.length(data[position + 1].startTime, data[position].startTime) *
                    holder.view.context.resources.getDimension(R.dimen.schedule_item_height)).toInt()
        }
        // Show tips if it's the first start (check via SharedPreferences)
        if (firstStart) {
            // firstStart = false
            val editor = preferences.edit()
            editor.putBoolean(Keys.PREF_FIRST, false)
            editor.apply()
            (activity as MainActivity).showTips(MaterialShowcaseView.Builder(activity)
                    .setTarget(holder.view.scheduleMainLayout)
                    .setDismissText(activity.getString(R.string.confirm_tip))
                    .setContentText(activity.getString(R.string.list_tip))
                    .withRectangleShape()
                    .singleUse("0")
                    .setDismissOnTouch(true)
                    .build()
            )
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemId(position: Int): Long = position.toLong()

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    companion object {
        internal fun lessonDrawable(type: String): Int {
            return when (type) {
                "LAB" -> R.drawable.time_lab
                "SEM" -> R.drawable.time_sem
                else -> R.drawable.time_lec
            }
        }
    }
}