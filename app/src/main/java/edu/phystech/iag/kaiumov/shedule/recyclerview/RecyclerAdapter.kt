package edu.phystech.iag.kaiumov.shedule.recyclerview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saber.stickyheader.stickyView.StickHeaderRecyclerView
import edu.phystech.iag.kaiumov.shedule.Keys
import edu.phystech.iag.kaiumov.shedule.R
import edu.phystech.iag.kaiumov.shedule.activities.EditActivity
import edu.phystech.iag.kaiumov.shedule.model.ScheduleItem
import edu.phystech.iag.kaiumov.shedule.model.TimeUtils
import kotlinx.android.synthetic.main.recycler_header.view.*
import kotlinx.android.synthetic.main.schedule_item.view.*


class RecyclerAdapter(private val activity: Activity, private val key: String) :
        StickHeaderRecyclerView<ScheduleItem, HeaderDataImpl>() {

    private var nightMode = false

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        nightMode = preferences.getBoolean(activity.resources.getString(R.string.pref_night_key), false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                as LayoutInflater
        return when (viewType) {
            HeaderDataImpl.HEADER ->
                HeaderViewHolder(layoutInflater.inflate(R.layout.recycler_header, parent, false))
            else ->
                ViewHolder(layoutInflater.inflate(R.layout.schedule_item, parent, false))
        }
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        header ?: return
        val headerDataImpl = getHeaderDataInPosition(headerPosition)
        header.headerText.text = headerDataImpl.text
        if (nightMode) {
            header.headerRoot.setBackgroundResource(R.drawable.bg_header_night)
        }
//        val imageView = header.findViewById<ImageView>(R.id.imageView)
//        val id = getDrawableId(headerDataImpl.day)
//        if (imageView.tag is Int && imageView.tag == id)
//            return
//        imageView.tag = id
//        imageView.tag = imageView.setImageResource(id)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bindView(getHeaderDataInPosition(position))
            is ViewHolder -> holder.bindView(getDataInPosition(position))
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindView(item: ScheduleItem) {
            val context = itemView.context
            itemView.time_layout.background = ContextCompat.getDrawable(context, lessonDrawable(item.type))
            itemView.name.text = item.name
            itemView.prof.text = item.prof
            itemView.place.text = item.place
            itemView.startTime.text = item.startTime
            itemView.endTime.text = item.endTime

            // Increase item height for longer classes
            itemView.scheduleMainLayout.layoutParams.height = (context.resources.getDimension(R.dimen.schedule_item_height) *
                    maxOf(item.length(), 1.1)).toInt()
            // Set on click listener to open edit activity
            itemView.scheduleMainLayout.setOnClickListener {
                val intent = Intent(context, EditActivity::class.java)
                intent.action = Keys.ACTION_EDIT
                intent.putExtra(Keys.ITEM, item)
                intent.putExtra(Keys.KEY, key)
                context.startActivity(intent)
            }

            val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
            val spaces = preferences.getBoolean(context.resources.getString(R.string.pref_spaces_key), false)
            // Create spaces

            if (spaces && item.tag != null && item.tag is ScheduleItem) {
                val nextItem = item.tag as ScheduleItem
                itemView.rootLayout.layoutParams.height = itemView.scheduleMainLayout.height +
                        (TimeUtils.length(nextItem.startTime, item.startTime) *
                                context.resources.getDimension(R.dimen.schedule_item_height)).toInt()
            }
        }
    }

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bindView(headerDataImpl: HeaderDataImpl) {
            itemView.headerText.text = headerDataImpl.text
            if (nightMode) {
                itemView.headerRoot.setBackgroundResource(R.drawable.bg_header_night)
            }
//            val id = getDrawableId(headerDataImpl.day)
//            if (itemView.imageView.tag is Int && itemView.imageView.tag == id)
//                return
//            itemView.imageView.tag = id
//            itemView.imageView.tag = itemView.imageView.setImageResource(id)
        }
    }

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