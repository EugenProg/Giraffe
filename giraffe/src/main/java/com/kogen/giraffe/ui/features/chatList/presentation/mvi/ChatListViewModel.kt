package com.kogen.giraffe.ui.features.chatList.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.kogen.giraffe.ui.common.mvi.BaseMviViewModel
import com.kogen.giraffe.ui.features.chatList.domain.useCases.ClearChatListUseCase
import com.kogen.giraffe.ui.features.chatList.domain.useCases.DeleteChatByIdUseCase
import com.kogen.giraffe.ui.features.chatList.domain.useCases.LoadChatListUseCase
import kotlinx.coroutines.launch
import kz.evko.kogen_di.annotations.KoGenViewModel

@KoGenViewModel
internal class ChatListViewModel(
    val loadChatListUseCase: LoadChatListUseCase,
    val clearChatListUseCase: ClearChatListUseCase,
    val deleteChatByIdUseCase: DeleteChatByIdUseCase,
) : BaseMviViewModel<ChatListAction, ChatListState, ChatListEffect>(
    ChatListState()
) {
    init {
        viewModelScope.launch {
            loadChatListUseCase.execute().collect { items ->
                updateState {
                    it.copy(chatList = items)
                }
            }
        }
    }

    override fun handleAction(action: ChatListAction) {
        when (action) {
            is ChatListAction.ClearHistory -> {
                updateState {
                    it.copy(showDeleteDialog = false)
                }
                wrappedRequest(
                    call = { clearChatListUseCase.execute() },
                )
            }

            is ChatListAction.DeleteChat -> {
                wrappedRequest(
                    call = { deleteChatByIdUseCase.execute(action.chatId) },
                )
            }

            is ChatListAction.ShowDeleteDialog -> {
                updateState {
                    it.copy(showDeleteDialog = true)
                }
            }

            is ChatListAction.HideDeleteDialog -> {
                updateState {
                    it.copy(showDeleteDialog = false)
                }
            }

            is ChatListAction.ShowChatDetails -> {
                emitEffect(ChatListEffect.NavigateToDetails(action.id))
            }
        }
    }
}