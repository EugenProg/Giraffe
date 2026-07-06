package com.kogen.giraffe.ui.features.chatDetails.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.kogen.giraffe.di.koGenViewModel
import com.kogen.giraffe.navigation.popBackSafety
import com.kogen.giraffe.ui.common.ScreenContainerWrapper
import com.kogen.giraffe.ui.features.chatDetails.presentation.mvi.ChatDetailsAction
import com.kogen.giraffe.ui.features.chatDetails.presentation.mvi.ChatDetailsEffect
import com.kogen.giraffe.ui.features.chatDetails.presentation.mvi.ChatDetailsViewModel
import kz.evko.navigation.annotation.KoGenScreen

@KoGenScreen
@Composable
fun ChatDetailsContainer(
    navController: NavHostController,
    chatId: String,
) {
    ScreenContainerWrapper(
        viewModel = koGenViewModel<ChatDetailsViewModel>(),
        onEffect = {
            when (it) {
                is ChatDetailsEffect.NavigateBack -> navController.popBackSafety()
            }
        },
        screenContent = { state, action ->
            LaunchedEffect(chatId) {
                action(ChatDetailsAction.LoadChatDetails(chatId))
            }

            ChatDetailsScreen(
                state = state,
                action = action,
            )
        }
    )
}