package com.kogen.giraffe.ui.common.presentation.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

fun String?.copyToClipboard(context: Context, title: String? = "") {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText(title, this))
}