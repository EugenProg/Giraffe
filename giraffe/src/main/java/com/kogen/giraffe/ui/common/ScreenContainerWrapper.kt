package com.kogen.giraffe.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kogen.giraffe.ui.common.mvi.BaseMviViewModel
import com.kogen.giraffe.ui.common.mvi.UiAction
import com.kogen.giraffe.ui.common.mvi.UiEffect
import com.kogen.giraffe.ui.common.mvi.UiState

@Composable
fun <S : UiState, A : UiAction, E : UiEffect> ScreenContainerWrapper(
    viewModel: BaseMviViewModel<A, S, E>,
    onEffect: ((E) -> Unit)? = null,
    screenContent: @Composable (S, (A) -> Unit) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            onEffect?.invoke(effect)
        }
    }

    screenContent(state) { action ->
        viewModel.dispatch(action)
    }
}