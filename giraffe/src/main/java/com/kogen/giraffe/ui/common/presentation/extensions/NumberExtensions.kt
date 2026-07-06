package com.kogen.giraffe.ui.common.presentation.extensions

import java.util.Calendar

fun Long.timestampToTime(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this

    return "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}:${
        calendar.get(Calendar.SECOND)}"
}