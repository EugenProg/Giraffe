package com.kogen.giraffe.ui.features.chatList.domain.useCases

import com.kogen.giraffe.ui.features.chatList.domain.service.ChatListService
import kz.evko.kogen_di.annotations.KoGenComponent

internal interface DeleteChatByIdUseCase {
    suspend fun execute(chatId: String)
}

@KoGenComponent
internal class DeleteChatByIdUseCaseImpl(
    private val service: ChatListService,
) : DeleteChatByIdUseCase {
    override suspend fun execute(chatId: String) {
        return service.deleteChatById(chatId)
    }
}