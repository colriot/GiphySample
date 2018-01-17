package xyz.ryabov.giphysample.ui.main

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_image.view.*
import xyz.ryabov.giphysample.R
import xyz.ryabov.giphysample.network.Gif
import xyz.ryabov.giphysample.util.ImageLoader

class ImageAdapter(
    activity: Activity,
    private val imageLoader: ImageLoader,
    private val itemClickListener: (View, Gif) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

  private val inflater = LayoutInflater.from(activity)
  private var showLoadingMore = true

  private var items = emptyList<Gif>()

  init {
    setHasStableIds(true)
  }

  fun replaceWith(items: List<Gif>) {
    this.items = items
    notifyDataSetChanged()
  }

  fun addWith(images: List<Gif>) {
    items += images
    notifyItemRangeInserted(items.size - images.size, images.size)
  }

  fun isEmpty(): Boolean = items.isEmpty()

  fun getDataItemCount(): Int = items.size

  override fun getItemId(position: Int): Long {
    // We can skip DiffUtils as our items aren't supposed to be moved or changed, only added or removed.
    // TODO But hashCodes my collide though.
    return if (position == items.size) Long.MAX_VALUE else items[position].id.hashCode().toLong()
  }

  override fun getItemCount(): Int = items.size + if (showLoadingMore) 1 else 0

  override fun getItemViewType(position: Int): Int = if (position == items.size) TYPE_LOADING else TYPE_IMAGE

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    when (viewType) {
      TYPE_IMAGE   -> {
        val vh = ImageViewHolder(inflater.inflate(R.layout.item_image, parent, false))
        vh.itemView.setOnClickListener {
          val pos = vh.adapterPosition
          if (pos != RecyclerView.NO_POSITION) {
            itemClickListener(vh.itemView, items[pos])
          }
        }
        return vh
      }
      TYPE_LOADING -> {
        val view = inflater.inflate(R.layout.item_loading, parent, false).apply {
          (layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
        }
        return LoadingViewHolder(view)
      }
      else         -> throw IllegalArgumentException("Unknown viewType: $viewType")
    }
  }

  override fun onBindViewHolder(vh: RecyclerView.ViewHolder, position: Int) {
    if (vh is ImageViewHolder) {
      vh.bind(items[position])
    }
  }

  inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val titleView = view.title
    private val imageView = view.image

    fun bind(image: Gif) {
      titleView.text = image.user?.fullName
      imageView.setImageSize(image)
      imageLoader.loadUrlInto(image.preview.url, imageView)
    }
  }

  inner class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

  companion object {
    const val TYPE_IMAGE = 0
    const val TYPE_LOADING = 1
  }
}
