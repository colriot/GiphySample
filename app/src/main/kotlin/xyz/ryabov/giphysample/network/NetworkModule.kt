package xyz.ryabov.giphysample.network

import android.content.Context
import android.os.Build
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.moshi.Moshi
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import xyz.ryabov.giphysample.API_HOST
import xyz.ryabov.giphysample.API_KEY
import xyz.ryabov.giphysample.BuildConfig
import java.io.File
import javax.inject.Singleton

@Module
class NetworkModule {
  private val HTTP_CACHE_SIZE = 1024 * 1024 * 4L // 4 MB
  private val IMG_CACHE_SIZE = 1024 * 1024 * 24L // 24 MB

  @Provides fun provideCacheDir(context: Context): File =
      if (BuildConfig.DEBUG) context.externalCacheDir else context.cacheDir

  @Provides @Singleton fun provideOkHttp(cacheDir: File, interceptor: Interceptor): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor { Timber.tag("OkHttp").d(it) }
        .setLevel(HttpLoggingInterceptor.Level.BODY)

    return OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .addInterceptor(loggingInterceptor)
        .cache(Cache(File(cacheDir, "http"), HTTP_CACHE_SIZE))
        .build()
  }

  @Provides @Singleton fun provideMoshi(): Moshi = Moshi.Builder().build()

  @Provides @Singleton fun provideRetrofit(okHttp: OkHttpClient, moshi: Moshi): Retrofit =
      Retrofit.Builder()
          .baseUrl(API_HOST)
          .addConverterFactory(EnvelopingConverter())
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .client(okHttp)
          .build()

  @Provides @Singleton fun provideApi(retrofit: Retrofit): Api = retrofit.create(Api::class.java)

  @Provides @Singleton fun provideRequestInterceptor() = Interceptor {
    val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"
    val userAgent = "GiphySample/${BuildConfig.VERSION_NAME}; Android ${Build.VERSION.RELEASE}; $deviceName"

    val newRequest = it.request().newBuilder().apply {
      url(it.request().url().newBuilder().addQueryParameter("api_key", API_KEY).build())

      addHeader("User-Agent", userAgent)
      addHeader("Accept", "application/json")
    }.build()

    it.proceed(newRequest)
  }

  @Provides @Singleton fun providePicasso(context: Context, cacheDir: File): Picasso {
    val loggingInterceptor = HttpLoggingInterceptor { Timber.tag("Picasso").d(it) }
        .setLevel(HttpLoggingInterceptor.Level.BASIC)

    val httpClient = OkHttpClient.Builder()
        .addNetworkInterceptor(loggingInterceptor)
        .cache(Cache(File(cacheDir, "images"), IMG_CACHE_SIZE))
        .build()

    val picasso = Picasso.Builder(context)
//        .indicatorsEnabled(BuildConfig.DEBUG) // Commented to not clutter UI.
        .downloader(OkHttp3Downloader(httpClient))
        .build()

    Picasso.setSingletonInstance(picasso)
    return picasso
  }
}
