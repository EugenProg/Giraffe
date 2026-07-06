package com.kogen.giraffe.ui.common.domain.models

import com.kogen.giraffe.db.entity.GiraffeMessageEntity

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
        textContent = this.textContent,
        filePath = this.filePath,
        timestamp = this.timestamp,
    )
}
