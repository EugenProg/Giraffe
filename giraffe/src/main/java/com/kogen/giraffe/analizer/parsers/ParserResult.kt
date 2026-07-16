package com.kogen.giraffe.analizer.parsers

import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

data class ParserResult(
    val contentType: GiraffeContentType,
    val filePath: String?,
    val bytes: ByteArray,
)
