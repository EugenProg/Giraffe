package com.kogen.giraffe.ui.features.chatDetails.presentation.mvi

import com.kogen.giraffe.ui.common.mvi.UiAction

sealed interface ChatDetailsAction : UiAction {
    data class LoadChatDetails(val id: String) : ChatDetailsAction
    data object NavigateBack : ChatDetailsAction
}