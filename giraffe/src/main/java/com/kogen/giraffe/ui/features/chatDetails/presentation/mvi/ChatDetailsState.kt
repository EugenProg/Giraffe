package com.kogen.giraffe.ui.features.chatDetails.presentation.mvi

import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.mvi.UiState
import com.kogen.giraffe.ui.common.presentation.AudioPlaybackState

internal data class ChatDetailsState(
    val chat: GiraffeChat? = null,
    val showRequestDetails: Boolean = false,
    val audioPlayback: AudioPlaybackState = AudioPlaybackState(),
): UiState