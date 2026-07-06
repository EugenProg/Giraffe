package com.kogen.giraffe.ui.common.domain.models

import com.kogen.giraffe.db.entity.ChatWithDetails

internal data class GiraffeChat(
    val id: String,
    val url: String,
    val methodShortName: String,
    val timestamp: Long,
    val status: GiraffeChatStatus,
    val headers: List<GiraffeHeader>,
    val messages: List<GiraffeMessage>,
)

internal fun ChatWithDetails.toDomain(): GiraffeChat {
    return GiraffeChat(
        id = this.chat.chatId,
        url = this.chat.url,
        methodShortName = this.chat.methodShortName,
        timestamp = this.chat.timestamp,
        status = this.chat.status,
        headers = this.headers.map { header ->
            header.toDomain()
        },
        messages = this.messages.map { message ->
            message.toDomain()
        }
    )
}