package com.kogen.giraffe.ui.common.presentation.extensions

import java.util.Calendar
import java.util.Date

fun Long.timestampToTime(): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this

    return "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}:${
        calendar.get(Calendar.SECOND)
    }"
}

fun Long.timestampToDateTime(): String {
    return Date(this).toString()
}

fun Int.msToDurationText(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}