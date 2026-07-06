package com.kogen.giraffe.ui.common.mvi

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface UiAction
interface UiState
interface UiEffect

abstract class BaseMviViewModel<A : UiAction, S : UiState, E : UiEffect>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun dispatch(action: A) {
        logAction(action)
        handleAction(action)
    }

    protected abstract fun handleAction(action: A)

    protected fun updateState(transform: (S) -> S) {
        _state.update(transform)
    }

    protected fun emitEffect(effect: E) {
        logEffect(effect)
        viewModelScope.launch { _effects.send(effect) }
    }

    private fun logAction(action: A) {
        Log.d("MVI_ACTION", "🚀 Action: ${action::class.simpleName}")
    }

    private fun logEffect(effect: E) {
        Log.d("MVI_EFFECT", "✨ Effect: ${effect::class.simpleName}")
    }

    protected fun <T> wrappedRequest(
        call: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = call()
                withContext(Dispatchers.Main) { onSuccess(result) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError(e) }
            }
        }
    }
}