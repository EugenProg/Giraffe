package com.kogen.giraffe.analizer

import android.content.Context
import com.kogen.giraffe.analizer.parsers.ContentParser
import com.kogen.giraffe.analizer.parsers.GiraffeAudioParser
import com.kogen.giraffe.analizer.parsers.GiraffeImageParser
import com.kogen.giraffe.analizer.parsers.GiraffeJsonParser
import com.kogen.giraffe.analizer.parsers.GiraffeUnknownBinaryParser
import com.kogen.giraffe.analizer.parsers.GiraffeVideoParser
import com.kogen.giraffe.db.entity.GiraffeContentType
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
        val transformedMessage = transformProtobufStringToValues(message)

        for (parser in allParsers) {
            val result = parser.parse(transformedMessage, context)
            if (result != null) return result
        }

        return AnalysisResult(
            contentType = GiraffeContentType.Unknown,
            textContent = transformedMessage.take(1000),
            filePath = null
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

            // 1. Очищаем кавычки и бэкслеши на концах строки
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.removeSurrounding("\"")
            }

            // 2. Исправляем экранированные внутренние кавычки (например, \\\"id\\\" -> "id")
            if (value.contains("\\\"")) {
                value = value.replace("\\\"", "\"")
            }

            // 3. Пытаемся положить значение в JSON в правильном формате
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
            } catch (e: Exception) {
                // Если упало (например, это просто текст со скобками), сохраняем как обычную строку
                jsonObject.put(key, value)
            }
        }

        return jsonObject.toString(2)
    }
}