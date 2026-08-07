package com.kogen.giraffe.ui.features.chatList.data.service

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.db.entity.ChatWithDetails
import com.kogen.giraffe.db.entity.GiraffeChatEntity
import com.kogen.giraffe.testutil.FakeGiraffeLogDao
import com.kogen.giraffe.ui.common.domain.models.GiraffeChatStatus
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ChatListServiceImplTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dao = FakeGiraffeLogDao()
    private val service = ChatListServiceImpl(dao)

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
    fun `loadChatList maps every emitted row to its domain model`() = runTest {
        dao.emitChats(listOf(chatWithDetails("chat-1"), chatWithDetails("chat-2")))

        service.loadChatList().test {
            val chats = awaitItem()
            assertThat(chats.map { it.id }).containsExactly("chat-1", "chat-2").inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadChatList reflects later updates from the dao`() = runTest {
        service.loadChatList().test {
            assertThat(awaitItem()).isEmpty()

            dao.emitChats(listOf(chatWithDetails("chat-1")))

            assertThat(awaitItem().map { it.id }).containsExactly("chat-1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteChats removes the rows and deletes their media files from disk`() = runTest {
        val mediaFile = tempFolder.newFile("orphaned_media.png")
        mediaFile.writeText("fake image bytes")
        dao.filePathsToReturn = listOf(mediaFile.absolutePath)

        service.deleteChats(listOf("chat-1"))

        assertThat(dao.deleteChatsByIdsCalls).containsExactly(listOf("chat-1"))
        assertThat(File(mediaFile.absolutePath).exists()).isFalse()
    }

    @Test
    fun `deleteChats does not throw when a referenced file is already gone`() = runTest {
        dao.filePathsToReturn = listOf(tempFolder.root.resolve("never_existed.png").absolutePath)

        service.deleteChats(listOf("chat-1"))

        assertThat(dao.deleteChatsByIdsCalls).containsExactly(listOf("chat-1"))
    }
}
