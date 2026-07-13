package com.kogen.giraffe.analizer

import android.content.Context
import android.util.Log
import com.google.protobuf.MessageLite
import com.kogen.giraffe.analizer.parsers.ContentParser
import com.kogen.giraffe.analizer.parsers.GiraffeAudioParser
import com.kogen.giraffe.analizer.parsers.GiraffeImageParser
import com.kogen.giraffe.analizer.parsers.GiraffeJsonParser
import com.kogen.giraffe.analizer.parsers.GiraffeUnknownBinaryParser
import com.kogen.giraffe.analizer.parsers.GiraffeVideoParser
import com.kogen.giraffe.analizer.parsers.ParserResult
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType
import kz.evko.kogen_di.annotations.KoGenComponent
import org.json.JSONArray
import org.json.JSONObject

@KoGenComponent(true)
class GiraffeMessageAnalyzer(
    private val context: Context,
) {

    companion object {
        private val customParsers = mutableListOf<ContentParser>()

        fun registerCustomParser(parser: ContentParser) {
            customParsers.add(parser)
        }
    }

    private val allParsers: List<ContentParser>
        get() {
            val mediaParsers = customParsers + listOf(
                GiraffeImageParser(),
                GiraffeAudioParser(),
                GiraffeVideoParser(),
                GiraffeUnknownBinaryParser(),
            )
            val jsonParser = GiraffeJsonParser(mediaParsers)

            return listOf(jsonParser) + mediaParsers
        }


    fun analyze(message: Any): AnalysisResult {
        val originalBytes =
            (message as? MessageLite)?.toByteArray() ?: message.toString().toByteArray()
        val textRepresentation = transformProtobufStringToValues(message)
        var parsingResult: ParserResult? = null

        for (parser in allParsers) {
            parser.parse(originalBytes, context)?.let {
                parsingResult = it
                break
            }
        }


        val trimmedStr = textRepresentation.trim()

        val isJson = ((trimmedStr.startsWith("{") && trimmedStr.endsWith("}")) ||
                (trimmedStr.startsWith("[") && trimmedStr.endsWith("]")))

        val readyText = when {
            isJson && parsingResult != null -> {
                transformProtobufStringToValues(
                    cutMediaFromString(
                        fullString = message.toString(),
                        mediaBytes = parsingResult.bytes,
                        placeholder = parsingResult.contentType.name,
                    )
                )
            }

            isJson -> textRepresentation
            else -> null
        }
//        logBytesAsHex(originalBytes)

        return AnalysisResult(
            contentType = parsingResult?.contentType
                ?: if (isJson) GiraffeContentType.Json else GiraffeContentType.Unknown,
            textContent = readyText ?: textRepresentation.take(1000),
            filePath = parsingResult?.filePath,
        )
    }

    private fun transformProtobufStringToValues(message: Any): String {
        val text = message.toString()
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }

        if (lines.none { it.contains(":") }) return text

        val jsonObject = JSONObject()

        for (line in lines) {
            if (line.startsWith("#")) continue

            val colonIndex = line.indexOf(":")
            if (colonIndex == -1) continue

            val key = line.substring(0, colonIndex).trim()
            var value = line.substring(colonIndex + 1).trim()

            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.removeSurrounding("\"")
            }

            if (value.contains("\\\"")) {
                value = value.replace("\\\"", "\"")
            }

            try {
                when {
                    value.startsWith("{") && value.endsWith("}") -> {
                        jsonObject.put(key, JSONObject(value))
                    }

                    value.startsWith("[") && value.endsWith("]") -> {
                        jsonObject.put(key, JSONArray(value))
                    }

                    else -> {
                        jsonObject.put(key, value)
                    }
                }
            } catch (_: Exception) {
                jsonObject.put(key, value)
            }
        }

        return jsonObject.toString(2)
    }

    fun escapeLikeProtobuf(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            when (v) {
                0x0A -> sb.append("\\n")
                0x0D -> sb.append("\\r")
                0x09 -> sb.append("\\t")
                0x22 -> sb.append("\\\"")
                0x27 -> sb.append("\\'")
                0x5C -> sb.append("\\\\")
                else -> if (v in 0x20..0x7E) {
                    sb.append(v.toChar())
                } else {
                    sb.append('\\')
                    sb.append(String.format("%03o", v))
                }
            }
        }
        return sb.toString()
    }

    fun cutMediaFromString(
        fullString: String,
        mediaBytes: ByteArray,
        placeholder: String,
        edgeSize: Int = 4
    ): String {
        if (mediaBytes.size < edgeSize * 2) {
            return fullString
        }

        val startBytes = mediaBytes.copyOfRange(0, edgeSize)
        val endBytes = mediaBytes.copyOfRange(mediaBytes.size - edgeSize, mediaBytes.size)

        val startEscaped = escapeLikeProtobuf(startBytes)
        val endEscaped = escapeLikeProtobuf(endBytes)

        val startIdx = fullString.indexOf(startEscaped)
        if (startIdx == -1) {
            Log.d(">>> cutMedia", "start pattern not found: $startEscaped")
            return fullString
        }

        val endIdx = fullString.lastIndexOf(endEscaped)
        if (endIdx == -1 || endIdx < startIdx) {
            Log.d(">>> cutMedia", "end pattern not found or before start: $endEscaped")
            return fullString
        }

        val cutTo = endIdx + endEscaped.length

        return fullString.substring(0, startIdx) + placeholder + fullString.substring(cutTo)
    }
}