package edu.phystech.iag.kaiumov.shedule

import android.os.AsyncTask
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import me.xdrop.fuzzywuzzy.FuzzySearch

class SearchTask(private val text: String,
                 private val keys: List<String>,
                 private val adapter: ArrayAdapter<String>) :
        AsyncTask<Void, Void, ArrayList<String>>() {

    companion object {
        private const val SEARCH_LIMIT = 10
    }

    override fun doInBackground(vararg p0: Void?): ArrayList<String> {
        val result = ArrayList<String>()
        FuzzySearch.extractTop(text, keys, SEARCH_LIMIT).forEach { result.add(it.string) }
        return result
    }

    override fun onPostExecute(result: ArrayList<String>) {
        adapter.clear()
        adapter.addAll(result)
        super.onPostExecute(result)
    }
}