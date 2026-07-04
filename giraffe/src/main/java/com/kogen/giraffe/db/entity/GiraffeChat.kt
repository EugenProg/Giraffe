package com.kogen.giraffe.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

const val CHAT_ID = "chatId"

@Entity(tableName = "giraffe_chat")
data class GiraffeChat(
    @PrimaryKey val chatId: String,
    val url: String,
    val methodShortName: String,
    val timestamp: Long,
    val status: GiraffeChatStatus,
)
