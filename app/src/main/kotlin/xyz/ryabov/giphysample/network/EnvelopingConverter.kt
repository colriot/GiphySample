package xyz.ryabov.giphysample.network

import com.squareup.moshi.Types
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

// Autounwrapping real API payload.
class EnvelopingConverter : Converter.Factory() {
  override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>,
                                     retrofit: Retrofit): Converter<ResponseBody, *> {

    val envelopeType = Types.newParameterizedType(Envelope::class.java, type)

    val delegate: Converter<ResponseBody, Envelope<*>> =
        retrofit.nextResponseBodyConverter(this, envelopeType, annotations)

    return Converter<ResponseBody, Any?> { value -> delegate.convert(value).data }
  }
}
