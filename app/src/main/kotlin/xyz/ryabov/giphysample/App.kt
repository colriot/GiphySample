package xyz.ryabov.giphysample

import android.content.Context
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.android.support.DaggerApplication
import timber.log.Timber
import xyz.ryabov.giphysample.network.NetworkModule
import xyz.ryabov.giphysample.ui.details.DetailsActivity
import xyz.ryabov.giphysample.ui.main.MainActivity
import javax.inject.Singleton

class App : DaggerApplication() {
  override fun onCreate() {
    super.onCreate()

    Timber.plant(Timber.DebugTree())
  }

  override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
    return DaggerAppComponent.builder().create(this)
  }
}

@Singleton
@Component(modules = [
  AndroidSupportInjectionModule::class,
  AndroidBindingModule::class,
  NetworkModule::class
])
interface AppComponent : AndroidInjector<App> {
  @Component.Builder
  abstract class Builder : AndroidInjector.Builder<App>()
}

@Module
interface AndroidBindingModule {
  @Binds fun bindContext(app: App): Context

  @ContributesAndroidInjector
  fun contributeMainActivityInjector(): MainActivity
  @ContributesAndroidInjector
  fun contributeDetailsActivityInjector(): DetailsActivity
}
