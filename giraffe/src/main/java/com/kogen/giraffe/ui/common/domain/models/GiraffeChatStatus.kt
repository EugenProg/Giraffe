package com.kogen.giraffe.ui.common.domain.models

import androidx.compose.ui.graphics.Color
import com.kogen.giraffe.ui.common.main.Error
import com.kogen.giraffe.ui.common.main.Primary
import com.kogen.giraffe.ui.common.main.Success

enum class GiraffeChatStatus {
    InProgress,
    Ok,
    Error,
}

internal fun GiraffeChatStatus.color(): Color {
    return when (this) {
        GiraffeChatStatus.InProgress -> Primary
        GiraffeChatStatus.Ok -> Success
        GiraffeChatStatus.Error -> Error
    }
}