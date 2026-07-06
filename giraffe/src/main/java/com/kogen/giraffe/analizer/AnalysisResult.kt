package com.kogen.giraffe.analizer

import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType

data class AnalysisResult(
    val contentType: GiraffeContentType,
    val textContent: String?,
    val filePath: String?,
)
