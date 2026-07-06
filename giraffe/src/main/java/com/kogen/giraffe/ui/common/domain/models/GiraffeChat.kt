package com.kogen.giraffe.ui.common.domain.models

internal data class GiraffeChat(
    val id: String,
    val url: String,
    val methodShortName: String,
    val timestamp: Long,
    val status: GiraffeChatStatus,
    val headers: List<GiraffeHeader>,
    val messages: List<GiraffeMessage>,
)