package xyz.ryabov.giphysample.ui.main

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import com.jakewharton.rxbinding2.support.v4.widget.refreshes
import com.jakewharton.rxbinding2.view.clicks
import com.uber.autodispose.kotlin.autoDisposeWith
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import xyz.ryabov.giphysample.R
import xyz.ryabov.giphysample.network.Gif
import xyz.ryabov.giphysample.ui.BaseActivity
import xyz.ryabov.giphysample.ui.details.DetailsActivity
import xyz.ryabov.giphysample.util.ImageLoader
import xyz.ryabov.giphysample.util.longToast
import xyz.ryabov.giphysample.widget.InfiniteScrollListener
import xyz.ryabov.giphysample.widget.StaggeredGridItemDecoration
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseActivity() {

  @Inject internal lateinit var vm: MainViewModel
  @Inject internal lateinit var imageLoader: ImageLoader

  private lateinit var adapter: ImageAdapter

  private val loadMoreEvents = PublishSubject.create<UiAction.LoadNextPage>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    adapter = ImageAdapter(this, imageLoader) { v, gif -> DetailsActivity.start(this, v, gif) }
    recycler.adapter = adapter
    val layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
    recycler.layoutManager = layoutManager
    recycler.addItemDecoration(StaggeredGridItemDecoration(resources.getDimensionPixelSize(R.dimen.spacing)))
    recycler.setHasFixedSize(true)

    recycler.addOnScrollListener(object : InfiniteScrollListener(layoutManager) {
      override fun onLoadMore() {
        val totalItemsCount = adapter.getDataItemCount()
        loadMoreEvents.onNext(UiAction.LoadNextPage(totalItemsCount))
      }
    })
  }

  override fun onStart() {
    super.onStart()
    bindToVm()
  }

  private fun bindToVm() {
    val loadActions = scope.lifecycle()
        .filter { it == Lifecycle.Event.ON_RESUME && adapter.isEmpty() }
        .map { Unit }
        .mergeWith(retryBtn.clicks())
        .map { UiAction.LoadAction }

    val reloadActions = refresher.refreshes().map { UiAction.ReloadAction }

    val loadMoreActions = loadMoreEvents.throttleFirst(600, TimeUnit.MILLISECONDS)

    Observable.merge(loadActions, reloadActions, loadMoreActions)
        .compose(vm.imagesTransformer())
        .autoDisposeWith(scope)
        .subscribe {
          if (it !is UiModel.Loading) {
            refresher.isRefreshing = false
          }

          when (it) {
            is UiModel.Loading        -> renderLoading()
            is UiModel.Initial        -> renderContent(it.images)
            is UiModel.NewPage        -> renderNewPage(it.images)

            is UiModel.Failure        -> {
              renderFailure()
              Timber.w(it.throwable)
            }
            is UiModel.NewPageFailure -> {
              renderFailureFloating()
              Timber.w(it.throwable)
            }
          }
        }
  }

  private fun renderLoading() {
    if (adapter.isEmpty()) {
      animator.displayedChild = 1
    } else {
      refresher.isRefreshing = true
    }
  }

  private fun renderContent(items: List<Gif>) {
    animator.displayedChild = 0
    adapter.replaceWith(items)
  }

  private fun renderNewPage(items: List<Gif>) {
    adapter.addWith(items)
  }

  private fun renderFailure() {
    if (adapter.isEmpty()) {
      animator.displayedChild = 2
    } else {
      renderFailureFloating()
    }
  }

  private fun renderFailureFloating() {
    longToast(getString(R.string.error_general))
  }
}
