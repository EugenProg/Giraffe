package com.kogen.giraffe.ui.features.chatList.data.service

import com.kogen.giraffe.db.dao.GiraffeLogDao
import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.domain.models.toDomain
import com.kogen.giraffe.ui.features.chatList.domain.service.ChatListService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kz.evko.kogen_di.annotations.KoGenComponent

@KoGenComponent(true)
internal class ChatListServiceImpl(
    val dao: GiraffeLogDao,
) : ChatListService {
    override suspend fun loadChatList(): Flow<List<GiraffeChat>> {
        return dao.getAllChatsWithDetails().map {
            it.map { details ->
                details.toDomain()
            }
        }
    }

    override suspend fun clearChatList() {
        dao.clearAllChats()
    }

    override suspend fun deleteChatById(chatId: String) {
        dao.deleteChatById(chatId)
    }
}