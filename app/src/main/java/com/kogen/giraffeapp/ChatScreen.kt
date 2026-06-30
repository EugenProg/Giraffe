package com.kogen.giraffeapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kogen.giraffeapp.di.koGenViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = koGenViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .statusBarsPadding()
                .padding(16.dp)
        ) {

            Text(
                "gRPC Chat Client",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            StatusBar(
                status = state.status,
                isSubscribed = state.isSubscribed,
                isChatActive = state.isChatActive
            )
            Spacer(Modifier.height(12.dp))

            ConnectionSection(
                serverHost = state.serverHost,
                serverPort = state.serverPort,
                roomId = state.roomId,
                clientId = state.clientId,
                isSubscribed = state.isSubscribed,
                isChatActive = state.isChatActive,
                onServerHostChange = viewModel::setServerHost,
                onServerPortChange = viewModel::setServerPort,
                onRoomIdChange = viewModel::setRoomId,
                onClientIdChange = viewModel::setClientId,
                onSubscribe = viewModel::subscribe,
                onUnsubscribe = viewModel::unsubscribe,
                onStartChat = viewModel::startChat,
                onLeave = viewModel::leaveRoom
            )
            Spacer(Modifier.height(12.dp))

            ActionRow(
                isChatActive = state.isChatActive,
                onPing = viewModel::sendPing,
                onClear = viewModel::clearMessages
            )
            Spacer(Modifier.height(8.dp))

            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            MessageList(messages = state.messages, modifier = Modifier.weight(1f))

            Spacer(Modifier.height(8.dp))
            MessageInput(
                text = state.inputText,
                enabled = state.isChatActive,
                onTextChange = viewModel::setInputText,
                onSend = viewModel::sendMessage
            )
        }
    }
}

@Composable
private fun StatusBar(status: String, isSubscribed: Boolean, isChatActive: Boolean) {
    val color = when {
        isChatActive -> Color(0xFF2E7D32)
        isSubscribed -> Color(0xFF1565C0)
        else -> Color(0xFF757575)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(10.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(status, fontSize = 13.sp, color = color)
    }
}

@Composable
private fun ConnectionSection(
    serverHost: String,
    serverPort: String,
    roomId: String,
    clientId: String,
    isSubscribed: Boolean,
    isChatActive: Boolean,
    onServerHostChange: (String) -> Unit,
    onServerPortChange: (String) -> Unit,
    onRoomIdChange: (String) -> Unit,
    onClientIdChange: (String) -> Unit,
    onSubscribe: () -> Unit,
    onUnsubscribe: () -> Unit,
    onStartChat: () -> Unit,
    onLeave: () -> Unit,
) {
    val isActive = isSubscribed || isChatActive
    Card(elevation = CardDefaults.cardElevation(2.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text("Параметры подключения", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            // Сервер
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = serverHost,
                    onValueChange = onServerHostChange,
                    label = { Text("IP сервера") },
                    placeholder = { Text("192.168.x.x", color = MaterialTheme.colorScheme.outline) },
                    singleLine = true,
                    enabled = !isActive,
                    modifier = Modifier.weight(3f)
                )
                OutlinedTextField(
                    value = serverPort,
                    onValueChange = onServerPortChange,
                    label = { Text("Порт") },
                    singleLine = true,
                    enabled = !isActive,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Комната и клиент
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = roomId,
                    onValueChange = onRoomIdChange,
                    label = { Text("Room ID") },
                    singleLine = true,
                    enabled = !isActive,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = clientId,
                    onValueChange = onClientIdChange,
                    label = { Text("Client ID") },
                    singleLine = true,
                    enabled = !isActive,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isSubscribed) {
                    Button(onClick = onSubscribe) { Text("Подписаться") }
                } else {
                    OutlinedButton(onClick = onUnsubscribe) { Text("Отписаться") }
                }

                if (!isChatActive) {
                    Button(onClick = onStartChat) { Text("Войти в чат") }
                } else {
                    OutlinedButton(
                        onClick = onLeave,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Выйти") }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(isChatActive: Boolean, onPing: () -> Unit, onClear: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onPing, enabled = isChatActive) { Text("Ping") }
        OutlinedButton(onClick = onClear) { Text("Очистить") }
    }
}

@Composable
private fun MessageList(messages: List<MessageItem>, modifier: Modifier) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    if (messages.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Сообщений нет", color = MaterialTheme.colorScheme.outline, fontSize = 14.sp)
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { msg -> MessageBubble(msg) }
        }
    }
}

@Composable
private fun MessageBubble(msg: MessageItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(msg.sender, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Text(msg.contentType, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                msg.content,
                fontSize = 13.sp,
                fontFamily = if (msg.contentType.contains("JSON")) FontFamily.Monospace else FontFamily.Default,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MessageInput(
    text: String,
    enabled: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("Сообщение") },
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onSend, enabled = enabled && text.isNotBlank()) {
            Text("Отправить")
        }
    }
}
