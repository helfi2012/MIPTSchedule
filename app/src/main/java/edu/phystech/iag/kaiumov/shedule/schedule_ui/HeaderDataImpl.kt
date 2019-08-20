package edu.phystech.iag.kaiumov.shedule.schedule_ui

import com.saber.stickyheader.stickyData.HeaderData
import androidx.annotation.LayoutRes


class HeaderDataImpl(val day: Int, val text: String,
                     @param:LayoutRes @field:LayoutRes private val layoutResource: Int) : HeaderData {

    @LayoutRes
    override fun getHeaderLayout(): Int {
        return layoutResource
    }

    override fun getHeaderType(): Int {
        return HEADER
    }

    companion object {
        const val HEADER = 1
    }
}