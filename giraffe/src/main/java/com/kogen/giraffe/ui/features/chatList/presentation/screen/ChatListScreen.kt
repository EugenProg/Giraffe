package com.kogen.giraffe.ui.features.chatList.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kogen.giraffe.R
import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.domain.models.color
import com.kogen.giraffe.ui.common.domain.models.icon
import com.kogen.giraffe.ui.common.main.BGSecondaryColor
import com.kogen.giraffe.ui.common.main.BackgroundColor
import com.kogen.giraffe.ui.common.main.ErrorColor
import com.kogen.giraffe.ui.common.main.PrimaryColor
import com.kogen.giraffe.ui.common.main.TextPrimaryColor
import com.kogen.giraffe.ui.common.presentation.NoContentView
import com.kogen.giraffe.ui.features.chatList.presentation.mvi.ChatListAction
import com.kogen.giraffe.ui.features.chatList.presentation.mvi.ChatListState

@Composable
internal fun ChatListScreen(
    state: ChatListState,
    action: (ChatListAction) -> Unit,
) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "\uD83E\uDD92 Giraffe(gRPC logger)",
                        style = TextStyle(
                            fontSize = 24.sp,
                        ),
                        color = TextPrimaryColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                action(ChatListAction.ShowDeleteDialog)
                            }
                            .padding(6.dp),
                        painter = painterResource(R.drawable.ic_trash),
                        contentDescription = null,
                        tint = TextPrimaryColor,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryColor)
                        .height(1.dp),
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            if (state.chatList.isNotEmpty()) {
                items(
                    items = state.chatList,
                    key = { chat -> chat.id },
                ) { chat ->
                    Box(
                        modifier = Modifier.animateItem()
                    ) {
                        ChatListItem(
                            chat = chat,
                            onClick = {
                                action(ChatListAction.ShowChatDetails(chat.id))
                            },
                        ) {
                            action(ChatListAction.DeleteChat(chat.id))
                        }
                    }
                }
            } else {
                item {
                    NoContentView()
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(chat: GiraffeChat, onClick: () -> Unit, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        SwipeToDismissBoxView(
            onDismiss = onDismiss,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundColor)
                    .clickable(onClick = onClick)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(chat.status.icon()),
                    contentDescription = null,
                    tint = chat.status.color(),
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier = Modifier
                        .weight(1f),
                ) {
                    Text(
                        text = chat.url,
                        style = TextStyle(
                            fontSize = 16.sp,
                        ),
                        color = TextPrimaryColor,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = chat.messages.lastOrNull()?.textContent.orEmpty(),
                        style = TextStyle(
                            fontSize = 14.sp,
                        ),
                        color = TextPrimaryColor,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BGSecondaryColor)
                .height(1.5.dp),
        )
    }
}

@Composable
private fun SwipeToDismissBoxView(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        SwipeToDismissBoxValue.Settled,
        SwipeToDismissBoxDefaults.positionalThreshold
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        onDismiss = {
            onDismiss()
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = ErrorColor,
                        shape = RoundedCornerShape(
                            bottomStart = 4.dp,
                            bottomEnd = 4.dp
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("DELETE")
            }
        },
        content = {
            content()
        },
    )
}