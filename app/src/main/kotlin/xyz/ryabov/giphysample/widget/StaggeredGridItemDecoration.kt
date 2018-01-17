package xyz.ryabov.giphysample.widget

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View

class StaggeredGridItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
    super.getItemOffsets(outRect, view, parent, state)

    val spanIndex = (view.layoutParams as StaggeredGridLayoutManager.LayoutParams).spanIndex
    val spanCount = (parent.layoutManager as StaggeredGridLayoutManager).spanCount
    val position = parent.getChildAdapterPosition(view)

    // Only for first column.
    if (spanIndex == 0) {
      outRect.left = spacing
    }

    // Only for first row.
    if (position < spanCount) {
      outRect.top = spacing
    }

    outRect.bottom = spacing
    outRect.right = spacing
  }
}
