package com.kogen.giraffe.analizer

import com.kogen.giraffe.db.entity.GiraffeContentType

data class AnalysisResult(
    val contentType: GiraffeContentType,
    val textContent: String?,
    val filePath: String?,
)
