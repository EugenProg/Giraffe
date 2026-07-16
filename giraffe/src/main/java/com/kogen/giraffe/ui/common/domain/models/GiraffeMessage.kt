package com.kogen.giraffe.ui.common.domain.models

import com.kogen.giraffe.db.entity.GiraffeMessageEntity
import org.json.JSONArray
import org.json.JSONObject

internal data class GiraffeMessage(
    val id: Long,
    val isIncoming: Boolean,
    val contentType: GiraffeContentType,
    val textContent: String?,
    val filePath: String?,
    val timestamp: Long,
)

internal fun GiraffeMessageEntity.toDomain(): GiraffeMessage {
    return GiraffeMessage(
        id = this.id,
        isIncoming = this.isIncoming,
        contentType = this.contentType,
        textContent = this.textContent.prettyPrintIfJson(),
        filePath = this.filePath,
        timestamp = this.timestamp,
    )
}

private fun String?.prettyPrintIfJson(): String? {
    if (this == null) return this

    val trimmed = this.trim()
    return try {
        when {
            trimmed.startsWith("{") -> JSONObject(trimmed).toString(2)
            trimmed.startsWith("[") -> JSONArray(trimmed).toString(2)
            else -> this
        }
    } catch (_: Exception) {
        this
    }
}
