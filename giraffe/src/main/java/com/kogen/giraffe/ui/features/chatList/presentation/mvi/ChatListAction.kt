package com.kogen.giraffe.ui.features.chatList.presentation.mvi

import com.kogen.giraffe.ui.common.mvi.UiAction

sealed interface ChatListAction: UiAction {
    data object ClearHistory: ChatListAction
    data class DeleteChat(val chatId: String): ChatListAction
    data object ShowDeleteDialog: ChatListAction
    data object HideDeleteDialog: ChatListAction
}