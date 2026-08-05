package com.kogen.giraffe.analizer

import android.content.Context
import android.util.Log
import com.google.protobuf.MessageLite
import com.kogen.giraffe.analizer.parsers.ContentParser
import com.kogen.giraffe.analizer.parsers.GiraffeAudioParser
import com.kogen.giraffe.analizer.parsers.GiraffeImageParser
import com.kogen.giraffe.analizer.parsers.GiraffeUnknownBinaryParser
import com.kogen.giraffe.analizer.parsers.GiraffeVideoParser
import com.kogen.giraffe.analizer.parsers.ParserResult
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType
import kz.evko.kogen_di.annotations.KoGenComponent
import org.json.JSONArray
import org.json.JSONObject

private const val MAX_DB_TEXT_LENGTH = 500_000

@KoGenComponent(true)
class GiraffeMessageAnalyzer(
    private val context: Context,
) {

    private val allParsers: List<ContentParser>
        get() = listOf(
            GiraffeImageParser(),
            GiraffeAudioParser(),
            GiraffeVideoParser(),
            GiraffeUnknownBinaryParser(),
        )


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
            textContent = truncateForDb(readyText) ?: textRepresentation.take(1000),
            filePath = parsingResult?.filePath,
        )
    }

    fun logBytesAsHex(bytes: ByteArray, tag: String = ">>> raw_bytes_hex", maxBytes: Int = 512) {
        val sb = StringBuilder()
        val limit = minOf(bytes.size, maxBytes)
        for (i in 0 until limit) {
            sb.append(String.format("%02x", bytes[i]))
            if ((i + 1) % 16 == 0) sb.append("\n") else sb.append(" ")
        }
        Log.d(tag, "size=${bytes.size}\n$sb")
    }

    fun truncateForDb(text: String?, maxLength: Int = MAX_DB_TEXT_LENGTH): String? {
        return when {
            text == null -> null
            text.length <= maxLength -> text
            else -> text.substring(0, maxLength)
        }
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

            val decoded = tryDecodeAsText(value)
            if (decoded != null) {
                value = decoded
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

    fun unescapeProtobufString(input: String): ByteArray {
        val result = mutableListOf<Byte>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            if (c == '\\' && i + 1 < input.length) {
                when (input[i + 1]) {
                    'n' -> {
                        result.add(0x0A); i += 2
                    }

                    'r' -> {
                        result.add(0x0D); i += 2
                    }

                    't' -> {
                        result.add(0x09); i += 2
                    }

                    '"' -> {
                        result.add(0x22); i += 2
                    }

                    '\'' -> {
                        result.add(0x27); i += 2
                    }

                    '\\' -> {
                        result.add(0x5C); i += 2
                    }

                    in '0'..'7' -> {
                        // до 3 восьмеричных цифр
                        var j = i + 1
                        var octal = ""
                        while (j < input.length && octal.length < 3 && input[j] in '0'..'7') {
                            octal += input[j]
                            j++
                        }
                        result.add(octal.toInt(8).toByte())
                        i = j
                    }

                    else -> {
                        result.add(c.code.toByte()); i++
                    }
                }
            } else {
                result.add(c.code.toByte())
                i++
            }
        }
        return result.toByteArray()
    }

    fun tryDecodeAsText(escapedValue: String): String? {
        val bytes = unescapeProtobufString(escapedValue)
        return try {
            val decoded = String(bytes, Charsets.UTF_8)
            val reEncoded = decoded.toByteArray(Charsets.UTF_8)
            if (reEncoded.contentEquals(bytes)) decoded else null
        } catch (_: Exception) {
            null
        }
    }

    fun escapeLikeProtobuf(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            when (val v = b.toInt() and 0xFF) {
                0x07 -> sb.append("\\a")
                0x08 -> sb.append("\\b")
                0x0A -> sb.append("\\n")
                0x0B -> sb.append("\\v")
                0x0C -> sb.append("\\f")
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
            return fullString
        }

        val endIdx = fullString.lastIndexOf(endEscaped)
        if (endIdx == -1 || endIdx < startIdx) {
            return fullString
        }

        val cutTo = endIdx + endEscaped.length

        return fullString.substring(0, startIdx) + placeholder + fullString.substring(cutTo)
    }
}