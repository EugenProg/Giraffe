package com.kogen.giraffe.analizer.parsers

import android.content.Context
import android.util.Log
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType
import org.json.JSONArray
import org.json.JSONObject

internal class GiraffeJsonParser(val mediaParsers: List<ContentParser>) : ContentParser {
    override fun parse(originalBytes: ByteArray, context: Context): ParserResult? {
//        val trimmedStr = message.trim()
//
//        if (!((trimmedStr.startsWith("{") && trimmedStr.endsWith("}")) ||
//                    (trimmedStr.startsWith("[") && trimmedStr.endsWith("]")))
//        ) {
//            return null
//        }
//
//        return try {
//            var foundContentType = GiraffeContentType.Json
//            var mainFilePath: String?
//
//            if (trimmedStr.startsWith("{")) {
//                val jsonObject = JSONObject(trimmedStr)
//                mainFilePath = scanJsonObject(jsonObject, originalBytes, context) { detectedType ->
//                    foundContentType = detectedType
//                }
//
//                AnalysisResult(
//                    contentType = foundContentType,
//                    textContent = jsonObject.toString(),
//                    filePath = mainFilePath
//                )
//            } else {
//                val jsonArray = JSONArray(trimmedStr)
//                mainFilePath = scanJsonArray(jsonArray, originalBytes, context) { detectedType ->
//                    foundContentType = detectedType
//                }
//
//                AnalysisResult(
//                    contentType = foundContentType,
//                    textContent = jsonArray.toString(),
//                    filePath = mainFilePath
//                )
//            }
//        } catch (_: Exception) {
//            null
//        }
        return null
    }

    private fun scanJsonObject(
        json: JSONObject,
        originalBytes: ByteArray,
        context: Context,
        onMediaFound: (GiraffeContentType) -> Unit
    ): String? {
        var firstFilePath: String? = null
        val keys = json.keys()
        Log.d(">>>", "scanJsonObject: $json")

        while (keys.hasNext()) {
            val key = keys.next()
            when (val value = json.get(key)) {
                is JSONObject -> {
                    val path = scanJsonObject(value, originalBytes, context, onMediaFound)
                    if (firstFilePath == null) firstFilePath = path
                }

                is JSONArray -> {
                    val path = scanJsonArray(value, originalBytes, context, onMediaFound)
                    if (firstFilePath == null) firstFilePath = path
                }

                is String -> {
                    for (parser in mediaParsers) {
                        val mediaResult = parser.parse(originalBytes, context)
                        if (mediaResult != null && mediaResult.filePath != null) {
                            Log.d(
                                "GiraffeDebug",
                                "Media detected in KEY: $key, parser: ${parser.javaClass}"
                            )
                            onMediaFound(mediaResult.contentType)
                            json.put(key, mediaResult.contentType.name)
                            if (firstFilePath == null) firstFilePath = mediaResult.filePath
                            break
                        }
                    }
                }
            }
        }
        return firstFilePath
    }

    private fun scanJsonArray(
        array: JSONArray,
        originalBytes: ByteArray,
        context: Context,
        onMediaFound: (GiraffeContentType) -> Unit
    ): String? {
        var firstFilePath: String? = null
        for (i in 0 until array.length()) {
            when (val value = array.get(i)) {
                is JSONObject -> {
                    val path = scanJsonObject(value, originalBytes, context, onMediaFound)
                    if (firstFilePath == null) firstFilePath = path
                }

                is JSONArray -> {
                    val path = scanJsonArray(value, originalBytes, context, onMediaFound)
                    if (firstFilePath == null) firstFilePath = path
                }

                is String -> {
                    for (parser in mediaParsers) {
                        val mediaResult = parser.parse(originalBytes, context)
                        if (mediaResult != null && mediaResult.filePath != null) {
                            Log.d("GiraffeDebug", "Media detected in ARRAY INDEX: $i")
                            onMediaFound(mediaResult.contentType)
                            array.put(i, mediaResult.contentType.name)
                            if (firstFilePath == null) firstFilePath = mediaResult.filePath
                            break
                        }
                    }
                }
            }
        }
        return firstFilePath
    }
}