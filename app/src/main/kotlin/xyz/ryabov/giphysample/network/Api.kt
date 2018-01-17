package xyz.ryabov.giphysample.network

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

  @GET("trending")
  fun getTrending(@Query("offset") offset: Int = 0, @Query("limit") limit: Int = 30): Single<List<Gif>>

  @GET("{id}")
  fun getById(@Path("id") id: String): Single<Gif>
}
