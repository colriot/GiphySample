package xyz.ryabov.giphysample.widget

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager

abstract class InfiniteScrollListener(private val layoutManager: StaggeredGridLayoutManager) : RecyclerView.OnScrollListener() {

  override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    // Skip scrolling upward.
    if (dy <= 0) return

    val visibleItemCount = recyclerView.childCount
    val totalItemCount = layoutManager.itemCount
    val firstVisibleItem = layoutManager.findFirstVisibleItemPositions(null).max() ?: 0

    if (totalItemCount - visibleItemCount - firstVisibleItem <= VISIBLE_THRESHOLD) {
      onLoadMore()
    }
  }

  abstract fun onLoadMore()

  companion object {
    // The minimum number of items remaining before we should loading more.
    private val VISIBLE_THRESHOLD = 10
  }
}
