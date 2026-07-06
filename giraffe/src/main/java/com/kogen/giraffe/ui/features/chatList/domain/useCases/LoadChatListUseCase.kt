package com.kogen.giraffe.ui.features.chatList.domain.useCases

import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.features.chatList.domain.service.ChatListService
import kotlinx.coroutines.flow.Flow
import kz.evko.kogen_di.annotations.KoGenComponent

internal interface LoadChatListUseCase {
    suspend fun execute(): Flow<List<GiraffeChat>>
}

@KoGenComponent
internal class LoadChatListUseCaseImpl(
    val service: ChatListService,
) : LoadChatListUseCase {
    override suspend fun execute(): Flow<List<GiraffeChat>> {
        return service.loadChatList()
    }
}