package com.kogen.giraffe.ui.features.chatList.presentation.mvi

import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.mvi.UiState

internal data class ChatListState(
    val chatList: List<GiraffeChat> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
) : UiState
