package xyz.ryabov.giphysample.network

import com.squareup.moshi.Json
import java.io.Serializable

// Let it all be Serializable for demo-simplicity.
data class Gif(
    @Json(name = "id") val id: String,
    @Json(name = "images") private val imagesStruct: ImagesStruct,
    @Json(name = "user") val user: User?
) : Serializable {
  val preview get() = imagesStruct.preview
}

data class ImagesStruct(
    @Json(name = "480w_still") val preview: Image
) : Serializable

data class Image(
    @Json(name = "url") val url: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int
) : Serializable

data class User(
    @Json(name = "profile_url") val profileUrl: String,
    @Json(name = "username") val username: String,
    @Json(name = "display_name") val fullName: String,
    @Json(name = "twitter") val twitter: String?
) : Serializable

data class Envelope<out T>(@Json(name = "data") val data: T)
