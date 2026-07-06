package com.kogen.giraffe.ui.common.domain.models

internal data class GiraffeHeader(
    val id: Long,
    val isResponse: Boolean,
    val key: String,
    val value: String,
)