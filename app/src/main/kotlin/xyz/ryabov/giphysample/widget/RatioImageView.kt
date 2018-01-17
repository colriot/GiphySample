package xyz.ryabov.giphysample.widget

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import xyz.ryabov.giphysample.network.Gif

class RatioImageView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {

  private var ratio: Float = 1f

  fun setImageSize(image: Gif) {
    ratio = image.preview.width.toFloat() / image.preview.height
    requestLayout()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val mode = MeasureSpec.getMode(widthMeasureSpec)
    if (mode != MeasureSpec.EXACTLY) {
      throw IllegalStateException("layout_width must be match_parent")
    }

    val width = MeasureSpec.getSize(widthMeasureSpec)
    val height = (width / ratio).toInt()
    val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
    super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
  }
}
