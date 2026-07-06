package com.kogen.giraffe.ui.features.chatDetails.presentation.mvi

import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.mvi.UiState

internal data class ChatDetailsState(
    val chat: GiraffeChat? = null,
): UiState