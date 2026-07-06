package com.kogen.giraffe.ui.features.chatDetails.presentation.mvi

import com.kogen.giraffe.ui.common.mvi.UiEffect

sealed interface ChatDetailsEffect: UiEffect {
    data object NavigateBack: ChatDetailsEffect
}