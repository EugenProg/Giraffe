package com.kogen.giraffe.ui.common.presentation.extensions

import android.net.Uri
import androidx.core.net.toUri
import com.kogen.giraffe.BuildConfig

internal fun String.transformFilePath(): Uri {
    val authority = BuildConfig.giraffeAuthority
    return "content://$authority/$this".toUri()
}