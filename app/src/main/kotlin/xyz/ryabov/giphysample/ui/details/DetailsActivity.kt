package xyz.ryabov.giphysample.ui.details

import android.app.Activity
import android.app.ActivityOptions
import android.arch.lifecycle.Lifecycle
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Pair
import android.view.View
import android.view.ViewTreeObserver
import com.uber.autodispose.kotlin.autoDisposeWith
import kotlinx.android.synthetic.main.activity_details.*
import xyz.ryabov.giphysample.R
import xyz.ryabov.giphysample.model.ImageModel
import xyz.ryabov.giphysample.network.Gif
import xyz.ryabov.giphysample.ui.BaseActivity
import xyz.ryabov.giphysample.util.ImageLoader
import xyz.ryabov.giphysample.util.longToast
import javax.inject.Inject

class DetailsActivity : BaseActivity() {

  @Inject internal lateinit var vm: DetailsViewModel
  @Inject internal lateinit var imageModel: ImageModel
  @Inject internal lateinit var imageLoader: ImageLoader

  private lateinit var customTabsIntent: CustomTabsIntent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_details)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    customTabsIntent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        .enableUrlBarHiding()
        .addDefaultShareMenuItem()
        .build()

    usernameView.movementMethod = LinkMovementMethod.getInstance()
    twitterView.movementMethod = LinkMovementMethod.getInstance()

    if (intent.hasExtra(EXTRA_IMAGE)) {
      val image = intent.getSerializableExtra(EXTRA_IMAGE) as Gif

      renderImage(image)
      setupTransition()
    } else if (intent.data != null) {
      val imageId = intent.data.lastPathSegment // TODO may want to check path structure to not map wrong urls.
      bindToVm(imageId)
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    super.onBackPressed() // NOTE: done on purpose, to keep backward SharedElement animation when Up clicked.
    return true
  }

  private fun bindToVm(imageId: String) {
    val loadActions = scope.lifecycle()
        .filter { it == Lifecycle.Event.ON_CREATE }
        .map { UiAction.LoadAction(imageId) }

    loadActions
        .compose(vm.loadImageById())
        .autoDisposeWith(scope)
        .subscribe {
          when (it) {
            is UiModel.Success -> renderImage(it.image)
            is UiModel.Failure -> longToast(getString(R.string.error_failed_url))
          }
        }
  }

  private fun renderImage(image: Gif) {
    imageLoader.loadUrlInto(image.preview.url, detailsImage)

    fullNameView.setText(R.string.details_no_user)

    image.user?.let { user ->
      fullNameView.text = user.fullName
      usernameView.text = user.username.makeLink(user.profileUrl)
      twitterView.text = user.twitter?.let { it.makeLink(getString(R.string.twitter_search_fmt, it.removePrefix("@"))) }
    }
  }

  private fun setupTransition() {
    postponeEnterTransition()

    val decor = window.decorView
    decor.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
      override fun onPreDraw(): Boolean {
        decor.viewTreeObserver.removeOnPreDrawListener(this)
        startPostponedEnterTransition()
        return true
      }
    })
  }

  private fun String.makeLink(url: String): CharSequence {
    return SpannableStringBuilder().append(this, urlSpan(url), Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
  }

  private fun urlSpan(url: String): ClickableSpan = object : ClickableSpan() {
    override fun onClick(widget: View) {
      customTabsIntent.launchUrl(this@DetailsActivity, Uri.parse(url))
    }
  }

  companion object {
    private const val EXTRA_IMAGE = "extra:image"

    fun start(context: Activity, imageView: View, image: Gif) {

      // Prevent SharedElement overlapping with these bars.
      val navigationBar = context.findViewById<View>(android.R.id.navigationBarBackground)
      val statusBar = context.findViewById<View>(android.R.id.statusBarBackground)
      val actionBar = context.findViewById<View>(R.id.toolbar)

      context.startActivity(
          Intent(context, DetailsActivity::class.java).putExtra(EXTRA_IMAGE, image),
          ActivityOptions.makeSceneTransitionAnimation(
              context,
              Pair.create(imageView, context.getString(R.string.transition_image)),
              Pair.create(statusBar, statusBar.transitionName),
              Pair.create(actionBar, actionBar.transitionName),
              Pair.create(navigationBar, navigationBar.transitionName)
          ).toBundle()
      )
    }
  }
}
