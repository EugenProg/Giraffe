package com.kogen.giraffe.ui.features.chatDetails.domain.useCases

import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.features.chatDetails.domain.service.ChatDetailsService
import kotlinx.coroutines.flow.Flow
import kz.evko.kogen_di.annotations.KoGenComponent

internal interface LoadChatDetailsUseCase {
    suspend fun execute(id: String): Flow<GiraffeChat?>
}

@KoGenComponent
internal class LoadChatDetailsUseCaseImpl(
    val service: ChatDetailsService,
) : LoadChatDetailsUseCase {
    override suspend fun execute(id: String): Flow<GiraffeChat?> {
        return service.loadChatDetails(id)
    }
}