package com.kogen.giraffe.ui.features.chatList.domain.useCases

import com.kogen.giraffe.ui.features.chatList.domain.service.ChatListService
import kz.evko.kogen_di.annotations.KoGenComponent

internal interface DeleteChatsByIdUseCase {
    suspend fun execute(chatIds: List<String>)
}

@KoGenComponent
internal class DeleteChatByIdUseCaseImpl(
    private val service: ChatListService,
) : DeleteChatsByIdUseCase {
    override suspend fun execute(chatIds: List<String>) {
        return service.deleteChats(chatIds)
    }
}