package com.example.explorexpert.adapters.observers

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.explorexpert.adapters.TripAdapter

class ScrollToTopObserver(
    private val recycler: RecyclerView
) : RecyclerView.AdapterDataObserver() {

    override fun onChanged() {
        recycler.scrollToPosition(0)
    }
    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        //recycler.scrollToPosition(0)
    }
    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        recycler.scrollToPosition(0)
    }
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        // Skip this to avoid losing scroll position when rotating device
        recycler.scrollToPosition(0)
    }
    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        recycler.scrollToPosition(0)
    }
    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
        recycler.scrollToPosition(0)
    }
}