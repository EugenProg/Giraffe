package com.kogen.giraffe.ui.common.domain.models

enum class GiraffeContentType {
    PlainText,
    Json,
    Image,
    Audio,
    Video,
    Unknown,
}

internal fun GiraffeContentType.type(): String {
    return when(this) {
        GiraffeContentType.PlainText -> "\uD83D\uDCDD"
        GiraffeContentType.Json -> "⚙\uFE0F"
        GiraffeContentType.Image -> "\uD83D\uDDBC\uFE0F"
        GiraffeContentType.Audio -> "\uD83C\uDFB5"
        GiraffeContentType.Video -> "\uD83C\uDFA5"
        GiraffeContentType.Unknown -> "\uD83D\uDCE6"
    }
}