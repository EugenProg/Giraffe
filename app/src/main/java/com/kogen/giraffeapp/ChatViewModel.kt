package com.kogen.giraffeapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kogen.testgrpc.proto.ClientCommand
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kz.evko.kogen_di.annotations.KoGenViewModel

data class ChatUiState(
    val serverHost: String = "192.168.1.102",
    val serverPort: String = "9090",
    val roomId: String = "room1",
    val clientId: String = "android-client-1",
    val isSubscribed: Boolean = false,
    val isChatActive: Boolean = false,
    val messages: List<MessageItem> = emptyList(),
    val inputText: String = "",
    val status: String = "Не подключено"
)

data class MessageItem(
    val sender: String,
    val content: String,
    val contentType: String
)

@KoGenViewModel
class ChatViewModel(private val chatClient: ChatClient) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var subscribeJob: Job? = null
    private var chatJob: Job? = null
    private val commands = MutableSharedFlow<ClientCommand>()

    fun setServerHost(value: String) = _uiState.update { it.copy(serverHost = value) }
    fun setServerPort(value: String) = _uiState.update { it.copy(serverPort = value) }
    fun setRoomId(value: String) = _uiState.update { it.copy(roomId = value) }
    fun setClientId(value: String) = _uiState.update { it.copy(clientId = value) }
    fun setInputText(value: String) = _uiState.update { it.copy(inputText = value) }

    fun subscribe() {
        val state = _uiState.value
        if (!ensureConnected(state)) return
        subscribeJob?.cancel()
        subscribeJob = viewModelScope.launch {
            _uiState.update { it.copy(isSubscribed = true, status = "Подписан на ${state.roomId}") }
            runCatching {
                chatClient.subscribe(state.roomId, state.clientId).collect { msg ->
                    _uiState.update { it.copy(messages = it.messages + msg.toItem()) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isSubscribed = false, status = "Ошибка: ${e.message}") }
            }
        }
    }

    fun unsubscribe() {
        subscribeJob?.cancel()
        _uiState.update { it.copy(isSubscribed = false, status = "Отписан") }
        disconnectIfIdle()
    }

    fun startChat() {
        val state = _uiState.value
        if (!ensureConnected(state)) return
        chatJob?.cancel()
        chatJob = viewModelScope.launch {
            _uiState.update { it.copy(isChatActive = true, status = "Чат активен в ${state.roomId}") }
            runCatching {
                chatClient.chat(commands).collect { msg ->
                    _uiState.update { it.copy(messages = it.messages + msg.toItem()) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isChatActive = false, status = "Ошибка чата: ${e.message}") }
            }
        }
        viewModelScope.launch {
            commands.emit(chatClient.joinCommand(state.roomId, state.clientId))
        }
    }

    fun leaveRoom() {
        val state = _uiState.value
        viewModelScope.launch {
            commands.emit(chatClient.leaveCommand(state.roomId, state.clientId))
        }
        chatJob?.cancel()
        _uiState.update { it.copy(isChatActive = false, status = "Вышел из комнаты") }
        disconnectIfIdle()
    }

    fun sendMessage() {
        val state = _uiState.value
        if (state.inputText.isBlank()) return
        viewModelScope.launch {
            commands.emit(chatClient.messageCommand(state.roomId, state.clientId, state.inputText))
        }
        _uiState.update { it.copy(inputText = "") }
    }

    fun sendPing() {
        val state = _uiState.value
        viewModelScope.launch {
            commands.emit(chatClient.pingCommand(state.roomId, state.clientId))
        }
    }

    fun clearMessages() = _uiState.update { it.copy(messages = emptyList()) }

    private fun ensureConnected(state: ChatUiState): Boolean {
        if (state.serverHost.isBlank()) {
            _uiState.update { it.copy(status = "Укажите IP сервера") }
            return false
        }
        val port = state.serverPort.toIntOrNull() ?: 9090
        chatClient.connect(state.serverHost.trim(), port)
        return true
    }

    private fun disconnectIfIdle() {
        val state = _uiState.value
        if (!state.isSubscribed && !state.isChatActive) {
            chatClient.disconnect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        subscribeJob?.cancel()
        chatJob?.cancel()
        chatClient.disconnect()
    }
}

private fun com.kogen.testgrpc.proto.ChatMessage.toItem() = MessageItem(
    sender = sender,
    content = when {
        hasText() -> text
        hasJsonObject() -> jsonObject
        hasJsonArray() -> jsonArray
        hasRawBytes() -> "[bytes: ${rawBytes.size()} B]"
        else -> ""
    },
    contentType = contentType
)
