package com.kogen.giraffe.ui.features.chatList.domain.service

import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import kotlinx.coroutines.flow.Flow

internal interface ChatListService {
    suspend fun loadChatList(): Flow<List<GiraffeChat>>
    suspend fun deleteChats(chatIds: List<String>)
}