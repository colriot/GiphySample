package xyz.ryabov.giphysample.util

import android.widget.ImageView
import com.squareup.picasso.Picasso
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageLoader @Inject constructor(private val picasso: Picasso) {
  fun loadUrlInto(url: String, imageView: ImageView) {
    picasso.load(url).into(imageView)
  }
}
