package com.kogen.giraffe.ui.common.domain.models

internal data class GiraffeMessage(
    val id: Long,
    val isIncoming: Boolean,
    val contentType: GiraffeContentType,
    val textContent: String?,
    val filePath: String?,
    val timestamp: Long,
)
