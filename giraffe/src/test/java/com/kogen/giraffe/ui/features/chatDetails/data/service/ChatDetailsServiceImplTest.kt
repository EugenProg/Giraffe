package com.kogen.giraffe.ui.features.chatDetails.data.service

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.db.entity.ChatWithDetails
import com.kogen.giraffe.db.entity.GiraffeChatEntity
import com.kogen.giraffe.testutil.FakeGiraffeLogDao
import com.kogen.giraffe.ui.common.domain.models.GiraffeChatStatus
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChatDetailsServiceImplTest {

    private val dao = FakeGiraffeLogDao()
    private val service = ChatDetailsServiceImpl(dao)

    private fun chatWithDetails(id: String) = ChatWithDetails(
        chat = GiraffeChatEntity(
            chatId = id,
            url = "host/Service/Method",
            methodShortName = "Method",
            timestamp = 1L,
            status = GiraffeChatStatus.Ok,
        ),
        headers = emptyList(),
        messages = emptyList(),
    )

    @Test
    fun `chatDetails starts out null before anything is loaded`() = runTest {
        service.chatDetails.test {
            assertThat(awaitItem()).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadChatDetails switches the stream to the requested chat`() = runTest {
        dao.emitChats(listOf(chatWithDetails("chat-1"), chatWithDetails("chat-2")))

        service.chatDetails.test {
            assertThat(awaitItem()).isNull()

            service.loadChatDetails("chat-2")

            assertThat(awaitItem()?.id).isEqualTo("chat-2")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `switching to a different id re-queries and follows that chat's own updates`() = runTest {
        dao.emitChats(listOf(chatWithDetails("chat-1")))

        service.chatDetails.test {
            assertThat(awaitItem()).isNull()

            service.loadChatDetails("chat-1")
            assertThat(awaitItem()?.id).isEqualTo("chat-1")

            service.loadChatDetails("chat-2")
            assertThat(awaitItem()).isNull() // chat-2 does not exist yet

            dao.emitChats(listOf(chatWithDetails("chat-1"), chatWithDetails("chat-2")))
            assertThat(awaitItem()?.id).isEqualTo("chat-2")

            cancelAndIgnoreRemainingEvents()
        }
    }
}
