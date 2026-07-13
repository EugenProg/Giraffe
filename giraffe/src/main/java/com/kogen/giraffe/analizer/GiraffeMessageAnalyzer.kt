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
import java.io.ByteArrayOutputStream

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
            val result = parser.parse(originalBytes, context)
            result?.let {
                parsingResult = it
//                val s = message.toString().substringAfter("raw_bytes: \"")
//                Log.d("TEST", s.take(100))
//
//                it.bytes.take(20).forEach {
//                    Log.d("TEST", "%02x".format(it))
//                }
                Log.d(">>>", replaceBinary(message.toString(), it.bytes, "ХУЙ"))
            }
        }

//        val encoded = removeBytesFromEscapedString(message.toString(), originalBytes)
//
//        val result = message.toString().replace(encoded, "[FILE_REMOVED]")
//        Log.d(">>>", encoded)

        val trimmedStr = textRepresentation.trim()

        val isJson = ((trimmedStr.startsWith("{") && trimmedStr.endsWith("}")) ||
                (trimmedStr.startsWith("[") && trimmedStr.endsWith("]")))

//        val readyText = when {
//            isJson && parsingResult != null -> {
//                findBytesInEscapedString(
//                    textRepresentation,
//                    parsingResult.bytes,
//                )
//            }
//            isJson -> textRepresentation
//            else -> null
//        }
//        Log.d(">>> ready text", readyText.orEmpty())

        return AnalysisResult(
            contentType = parsingResult?.contentType ?: GiraffeContentType.Unknown,
            textContent = textRepresentation.take(1000),
            null,
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

    fun replaceBinary(
        text: String,
        bytes: ByteArray,
        replacement: String = ""
    ): String {
        var start = -1
        var byteIndex = 0
        var pos = 0

        while (pos < text.length) {
            val current = pos
            val decoded = decodeByte(text, pos)

            if (decoded.first == bytes[byteIndex]) {
                if (byteIndex == 0) start = current

                byteIndex++

                if (byteIndex == bytes.size) {
                    return text.substring(0, start) +
                            replacement +
                            text.substring(decoded.second)
                }
            } else {
                byteIndex = 0
                start = -1
            }

            pos = decoded.second
        }

        return text
    }

    fun decodeByte(text: String, pos: Int): Pair<Byte, Int> {
        if (text[pos] != '\\') {
            return text[pos].code.toByte() to pos + 1
        }

        val next = text[pos + 1]

        return when {
            next in '0'..'7' -> {
                var end = pos + 1
                while (end < text.length &&
                    end < pos + 4 &&
                    text[end] in '0'..'7'
                ) end++

                text.substring(pos + 1, end).toInt(8).toByte() to end
            }

            next == 'n' -> '\n'.code.toByte() to pos + 2
            next == 'r' -> '\r'.code.toByte() to pos + 2
            next == 't' -> '\t'.code.toByte() to pos + 2
            next == '\\' -> '\\'.code.toByte() to pos + 2
            next == '"' -> '"'.code.toByte() to pos + 2

            else -> next.code.toByte() to pos + 2
        }
    }
}