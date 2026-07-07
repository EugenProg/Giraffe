package com.kogen.giraffe.ui.common.domain.models

import androidx.compose.ui.graphics.Color
import com.kogen.giraffe.R
import com.kogen.giraffe.ui.common.main.BGSecondaryColor
import com.kogen.giraffe.ui.common.main.BGTertiaryColor
import com.kogen.giraffe.ui.common.main.ErrorColor
import com.kogen.giraffe.ui.common.main.PrimaryColor
import com.kogen.giraffe.ui.common.main.SuccessColor

enum class GiraffeChatStatus {
    InProgress,
    Ok,
    Error,
    Interrupted,
}

internal fun GiraffeChatStatus.color(): Color {
    return when (this) {
        GiraffeChatStatus.InProgress -> BGTertiaryColor
        GiraffeChatStatus.Ok -> SuccessColor
        GiraffeChatStatus.Error -> ErrorColor
        GiraffeChatStatus.Interrupted -> PrimaryColor
    }
}

internal fun GiraffeChatStatus.icon(): Int {
    return when(this) {
        GiraffeChatStatus.InProgress -> R.drawable.ic_duration
        GiraffeChatStatus.Ok -> R.drawable.ic_complete
        GiraffeChatStatus.Error -> R.drawable.ic_error
        GiraffeChatStatus.Interrupted -> R.drawable.ic_warning
    }
}