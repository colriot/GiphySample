package xyz.ryabov.giphysample.ui

import android.arch.lifecycle.Lifecycle.Event
import com.uber.autodispose.LifecycleScopeProvider
import com.uber.autodispose.android.lifecycle.scope
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.include_toolbar.*
import kotlin.LazyThreadSafetyMode.NONE

abstract class BaseActivity : DaggerAppCompatActivity() {
  @Suppress("UNCHECKED_CAST")
  protected val scope by lazy(NONE) { scope() as LifecycleScopeProvider<Event> }

  override fun onContentChanged() {
    super.onContentChanged()
    toolbar?.let {
      setSupportActionBar(it)
    }
  }
}
