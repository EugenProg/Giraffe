package com.kogen.giraffe.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ChatWithDetails(
    @Embedded val call: GiraffeChat,

    @Relation(parentColumn = CHAT_ID, entityColumn = CHAT_ID)
    val headers: List<GiraffeHeader>,

    @Relation(parentColumn = CHAT_ID, entityColumn = CHAT_ID)
    val messages: List<GiraffeMessage>,
)
