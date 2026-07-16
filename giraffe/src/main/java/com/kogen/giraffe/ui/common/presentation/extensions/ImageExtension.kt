package com.kogen.giraffe.ui.common.presentation.extensions

import android.graphics.BitmapFactory

fun decodeImageAspectRatio(path: String): Float? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, options)
    if (options.outWidth <= 0 || options.outHeight <= 0) return null
    return options.outWidth.toFloat() / options.outHeight.toFloat()
}