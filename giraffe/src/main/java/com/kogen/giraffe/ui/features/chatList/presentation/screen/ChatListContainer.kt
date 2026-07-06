package com.kogen.giraffe.ui.features.chatList.presentation.screen

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.kogen.giraffe.di.koGenViewModel
import com.kogen.giraffe.ui.common.ScreenContainerWrapper
import com.kogen.giraffe.ui.common.presentation.GiraffeAlert
import com.kogen.giraffe.ui.common.presentation.GiraffeButtonData
import com.kogen.giraffe.ui.common.presentation.GiraffeButtonStyle
import com.kogen.giraffe.ui.features.chatList.presentation.mvi.ChatListAction
import com.kogen.giraffe.ui.features.chatList.presentation.mvi.ChatListViewModel
import kz.evko.navigation.annotation.KoGenScreen

@KoGenScreen(startDestination = true)
@Composable
fun ChatListContainer(
    navController: NavHostController,
) {
    ScreenContainerWrapper(
        viewModel = koGenViewModel<ChatListViewModel>(),
        screenContent = { state, action ->
            if (state.showDeleteDialog) {
                GiraffeAlert(
                    title = "Удалить всю историю?",
                    description = "Это безвозвратно удалит все перехваченные gRPC-соединения и чаты из локальной базы данных",
                    confirmButton = GiraffeButtonData(
                        title = "Удалить",
                        style = GiraffeButtonStyle.NegativeType,
                        onClick = {
                            action(ChatListAction.ClearHistory)
                        }
                    ),
                    cancelButton = GiraffeButtonData(
                        title = "Отмена",
                        style = GiraffeButtonStyle.SecondaryType,
                        onClick = {
                            action(ChatListAction.HideDeleteDialog)
                        }
                    )
                )
            }

            ChatListScreen(
                state = state,
                action = action,
            )
        }
    )
}