package com.kogen.giraffe.ui.features.chatList.domain.useCases

import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.features.chatList.domain.service.ChatListService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChatListUseCasesTest {

    @Test
    fun `LoadChatListUseCase forwards the service's flow untouched`() = runTest {
        val chats = listOf<GiraffeChat>()
        val service = mockk<ChatListService> {
            coEvery { loadChatList() } returns flowOf(chats)
        }

        val result = LoadChatListUseCaseImpl(service).execute()

        assertThat(result.first()).isSameInstanceAs(chats)
        coVerify(exactly = 1) { service.loadChatList() }
    }

    @Test
    fun `DeleteChatsByIdUseCase forwards the id list to the service unchanged`() = runTest {
        val service = mockk<ChatListService>(relaxed = true)
        val ids = listOf("chat-1", "chat-2")

        DeleteChatByIdUseCaseImpl(service).execute(ids)

        coVerify(exactly = 1) { service.deleteChats(ids) }
    }
}
