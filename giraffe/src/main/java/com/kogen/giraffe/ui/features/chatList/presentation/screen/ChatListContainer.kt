package com.kogen.giraffe.ui.features.chatList.presentation.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.kogen.giraffe.di.koGenViewModel
import com.kogen.giraffe.navigation.ActionToChatDetailsContainer
import com.kogen.giraffe.navigation.navigateSafety
import com.kogen.giraffe.ui.common.ScreenContainerWrapper
import com.kogen.giraffe.ui.features.chatList.presentation.mvi.ChatListEffect
import com.kogen.giraffe.ui.features.chatList.presentation.mvi.ChatListViewModel
import kz.evko.navigation.annotation.KoGenScreen

@KoGenScreen(startDestination = true)
@Composable
fun ChatListContainer(
    navController: NavHostController,
) {
    ScreenContainerWrapper(
        viewModel = koGenViewModel<ChatListViewModel>(),
        onEffect = {
            when (it) {
                is ChatListEffect.NavigateToDetails -> {
                    navController.navigateSafety(ActionToChatDetailsContainer(it.id))
                }
            }
        },
        screenContent = { state, action ->
            ChatListScreen(
                state = state,
                action = action,
            )
        }
    )
}