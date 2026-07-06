package com.kogen.giraffe.ui.common.domain.models

import androidx.compose.ui.graphics.Color
import com.kogen.giraffe.ui.common.main.ErrorColor
import com.kogen.giraffe.ui.common.main.PrimaryColor
import com.kogen.giraffe.ui.common.main.SuccessColor

enum class GiraffeChatStatus {
    InProgress,
    Ok,
    Error,
}

internal fun GiraffeChatStatus.color(): Color {
    return when (this) {
        GiraffeChatStatus.InProgress -> PrimaryColor
        GiraffeChatStatus.Ok -> SuccessColor
        GiraffeChatStatus.Error -> ErrorColor
    }
}