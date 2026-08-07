package com.kogen.giraffe.ui.features.chatDetails.domain.useCases

import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.domain.models.GiraffeChatStatus
import com.kogen.giraffe.ui.features.chatDetails.domain.service.ChatDetailsService
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoadChatDetailsUseCaseTest {

    @Test
    fun `exposes the service's chatDetails flow as-is`() = runTest {
        val chat = GiraffeChat(
            id = "chat-1",
            url = "host/Service/Method",
            methodShortName = "Method",
            timestamp = 1L,
            status = GiraffeChatStatus.Ok,
            headers = emptyList(),
            messages = emptyList(),
        )
        val service = mockk<ChatDetailsService> {
            every { chatDetails } returns flowOf(chat)
        }

        val result = LoadChatDetailsUseCaseImpl(service).chatDetails.first()

        assertThat(result).isSameInstanceAs(chat)
    }

    @Test
    fun `execute forwards the requested id to the service`() = runTest {
        val service = mockk<ChatDetailsService>(relaxed = true)

        LoadChatDetailsUseCaseImpl(service).execute("chat-42")

        coVerify(exactly = 1) { service.loadChatDetails("chat-42") }
    }
}
