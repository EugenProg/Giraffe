package com.kogen.giraffe.ui.features.chatDetails.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.kogen.giraffe.ui.common.mvi.BaseMviViewModel
import com.kogen.giraffe.ui.features.chatDetails.domain.useCases.LoadChatDetailsUseCase
import kotlinx.coroutines.launch
import kz.evko.kogen_di.annotations.KoGenViewModel

@KoGenViewModel
internal class ChatDetailsViewModel(
    private val loadChatDetailsUseCase: LoadChatDetailsUseCase,
) :
    BaseMviViewModel<ChatDetailsAction, ChatDetailsState, ChatDetailsEffect>(
        ChatDetailsState()
    ) {

    init {
        viewModelScope.launch {
            loadChatDetailsUseCase.chatDetails.collect { chat ->
                updateState {
                    it.copy(chat = chat)
                }
            }
        }
    }

    override fun handleAction(action: ChatDetailsAction) {
        when (action) {
            is ChatDetailsAction.LoadChatDetails -> {
                wrappedRequest(
                    call = { loadChatDetailsUseCase.execute(action.id) },
                )
            }

            is ChatDetailsAction.NavigateBack -> {
                emitEffect(ChatDetailsEffect.NavigateBack)
            }

            is ChatDetailsAction.ShowRequestDetail -> {
                updateState {
                    it.copy(showRequestDetails = true)
                }
            }

            is ChatDetailsAction.HideRequestDetail -> {
                updateState {
                    it.copy(showRequestDetails = false)
                }
            }
        }
    }
}