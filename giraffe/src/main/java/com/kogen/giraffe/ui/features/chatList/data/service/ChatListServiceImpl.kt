package com.kogen.giraffe.ui.features.chatList.data.service

import com.kogen.giraffe.db.dao.GiraffeLogDao
import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.domain.models.GiraffeHeader
import com.kogen.giraffe.ui.common.domain.models.GiraffeMessage
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
                GiraffeChat(
                    id = details.chat.chatId,
                    url = details.chat.url,
                    methodShortName = details.chat.methodShortName,
                    timestamp = details.chat.timestamp,
                    status = details.chat.status,
                    headers = details.headers.map { header ->
                        GiraffeHeader(
                            id = header.id,
                            isResponse = header.isResponse,
                            key = header.key,
                            value = header.value
                        )
                    },
                    messages = details.messages.map { message ->
                        GiraffeMessage(
                            id = message.id,
                            isIncoming = message.isIncoming,
                            contentType = message.contentType,
                            textContent = message.textContent,
                            filePath = message.filePath,
                            timestamp = message.timestamp,
                        )
                    }
                )
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