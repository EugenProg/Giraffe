package com.kogen.giraffe.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "giraffe_messages",
    foreignKeys = [
        ForeignKey(
            entity = GiraffeChat::class,
            parentColumns = [CHAT_ID],
            childColumns = [CHAT_ID],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = [CHAT_ID])]
)
data class GiraffeMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: String,
    val isIncoming: Boolean,
    val contentType: GiraffeContentType,
    val textContent: String?,
    val filePath: String?,
    val timestamp: Long,
)
