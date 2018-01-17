@file:Suppress("NOTHING_TO_INLINE")

package xyz.ryabov.giphysample.util

import android.content.Context
import android.widget.Toast
import io.reactivex.Observable

inline fun Context.toast(message: String) {
  Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

inline fun Context.longToast(message: String) {
  Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

inline fun <reified R : Any> Observable<*>.ofType(): Observable<R> = ofType(R::class.java)
