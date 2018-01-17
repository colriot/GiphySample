package xyz.ryabov.giphysample.model

import io.reactivex.Single
import xyz.ryabov.giphysample.network.Api
import xyz.ryabov.giphysample.network.Gif
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageModel @Inject constructor(private val api: Api) {
  private val items = arrayListOf<Gif>()

  // Return preloaded if any.
  fun getInitial(): Single<List<Gif>> = if (items.isEmpty()) fetchImages() else Single.just(items)

  fun fetchImages(offset: Int = 0): Single<List<Gif>> {
    return api.getTrending(offset)
        .doOnSuccess {
          if (offset == 0) {
            items.clear()
          }
          items += it
        }
  }

  // No big gain in searching through the local cache while it's used only for deeplinks.
  fun getById(id: String) = api.getById(id)
}
