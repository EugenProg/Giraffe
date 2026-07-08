package com.kogen.giraffe.analizer.parsers

import android.content.Context
import com.kogen.giraffe.analizer.AnalysisResult

interface ContentParser {
    fun parse(message: String, originalBytes: ByteArray, context: Context): AnalysisResult?
}