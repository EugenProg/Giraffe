package com.kogen.giraffe.ui.features.chatList.presentation.mvi

import android.util.Log
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.domain.models.GiraffeChatStatus
import com.kogen.giraffe.ui.features.chatList.domain.useCases.DeleteChatsByIdUseCase
import com.kogen.giraffe.ui.features.chatList.domain.useCases.LoadChatListUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class ChatListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val chats = MutableStateFlow<List<GiraffeChat>>(emptyList())
    private val loadChatListUseCase = mockk<LoadChatListUseCase> {
        coEvery { execute() } returns chats
    }
    private val deleteChatsByIdUseCase = mockk<DeleteChatsByIdUseCase>(relaxed = true)

    private fun chat(id: String, status: GiraffeChatStatus = GiraffeChatStatus.Ok) = GiraffeChat(
        id = id,
        url = "host/Service/Method",
        methodShortName = "Method",
        timestamp = 1L,
        status = status,
        headers = emptyList(),
        messages = emptyList(),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        Dispatchers.resetMain()
    }

    private fun viewModel() = ChatListViewModel(loadChatListUseCase, deleteChatsByIdUseCase)

    @Test
    fun `initial load populates the chat list from the use case`() = runTest(dispatcher) {
        chats.value = listOf(chat("chat-1"), chat("chat-2"))

        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        assertThat(vm.state.value.chatList.map { it.id }).containsExactly("chat-1", "chat-2")
    }

    @Test
    fun `SelectChat adds and removes ids from the selection`() = runTest(dispatcher) {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.dispatch(ChatListAction.SelectChat("chat-1", isSelected = true))
        assertThat(vm.state.value.selectedIds).containsExactly("chat-1")

        vm.dispatch(ChatListAction.SelectChat("chat-2", isSelected = true))
        assertThat(vm.state.value.selectedIds).containsExactly("chat-1", "chat-2")

        vm.dispatch(ChatListAction.SelectChat("chat-1", isSelected = false))
        assertThat(vm.state.value.selectedIds).containsExactly("chat-2")
    }

    @Test
    fun `SelectAllChats selects every chat that is not still in progress`() = runTest(dispatcher) {
        chats.value = listOf(
            chat("done", GiraffeChatStatus.Ok),
            chat("failed", GiraffeChatStatus.Error),
            chat("pending", GiraffeChatStatus.InProgress),
        )
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.dispatch(ChatListAction.SelectAllChats)

        assertThat(vm.state.value.selectedIds).containsExactly("done", "failed")
    }

    @Test
    fun `UnSelectAllChats clears the current selection`() = runTest(dispatcher) {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()
        vm.dispatch(ChatListAction.SelectChat("chat-1", isSelected = true))

        vm.dispatch(ChatListAction.UnSelectAllChats)

        assertThat(vm.state.value.selectedIds).isEmpty()
    }

    @Test
    fun `ShowChatDetails emits a navigation effect with the chat id`() = runTest(dispatcher) {
        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()

        vm.effects.test {
            vm.dispatch(ChatListAction.ShowChatDetails("chat-7"))
            assertThat(awaitItem()).isEqualTo(ChatListEffect.NavigateToDetails("chat-7"))
        }
    }

    @Test
    fun `DeleteChats deletes exactly the currently selected ids`() = runTest(dispatcher) {
        // wrappedRequest hops onto the real Dispatchers.IO, so waiting on the test scheduler
        // alone can't observe it - a plain latch gives a dispatcher-agnostic sync point instead.
        val invoked = CountDownLatch(1)
        coEvery { deleteChatsByIdUseCase.execute(any()) } coAnswers { invoked.countDown() }

        val vm = viewModel()
        dispatcher.scheduler.advanceUntilIdle()
        vm.dispatch(ChatListAction.SelectChat("chat-1", isSelected = true))
        vm.dispatch(ChatListAction.SelectChat("chat-3", isSelected = true))

        vm.dispatch(ChatListAction.DeleteChats)

        assertThat(invoked.await(2, TimeUnit.SECONDS)).isTrue()
        coVerify(exactly = 1) { deleteChatsByIdUseCase.execute(listOf("chat-1", "chat-3")) }
    }
}
