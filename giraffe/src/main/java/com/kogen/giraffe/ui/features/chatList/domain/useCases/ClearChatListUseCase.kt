package com.kogen.giraffe.ui.features.chatList.domain.useCases

import com.kogen.giraffe.ui.features.chatList.domain.service.ChatListService
import kz.evko.kogen_di.annotations.KoGenComponent

internal interface ClearChatListUseCase {
    suspend fun execute()
}

@KoGenComponent
internal class ClearChatListUseCaseImpl(
    val service: ChatListService,
) : ClearChatListUseCase {
    override suspend fun execute() {
        return service.clearChatList()
    }
}