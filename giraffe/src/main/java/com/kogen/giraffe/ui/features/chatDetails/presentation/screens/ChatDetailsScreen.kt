package com.kogen.giraffe.ui.features.chatDetails.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kogen.giraffe.R
import com.kogen.giraffe.ui.common.domain.models.GiraffeChat
import com.kogen.giraffe.ui.common.domain.models.GiraffeChatStatus
import com.kogen.giraffe.ui.common.domain.models.GiraffeContentType
import com.kogen.giraffe.ui.common.domain.models.GiraffeMessage
import com.kogen.giraffe.ui.common.main.BGSecondaryColor
import com.kogen.giraffe.ui.common.main.BackgroundColor
import com.kogen.giraffe.ui.common.main.PrimaryColor
import com.kogen.giraffe.ui.common.main.TextPrimaryColor
import com.kogen.giraffe.ui.common.presentation.NoContentView
import com.kogen.giraffe.ui.common.presentation.extensions.copyToClipboard
import com.kogen.giraffe.ui.common.presentation.extensions.decodeImageAspectRatio
import com.kogen.giraffe.ui.common.presentation.extensions.timestampToDateTime
import com.kogen.giraffe.ui.common.presentation.extensions.timestampToTime
import com.kogen.giraffe.ui.features.chatDetails.presentation.mvi.ChatDetailsAction
import com.kogen.giraffe.ui.features.chatDetails.presentation.mvi.ChatDetailsState
import java.io.File

@Composable
internal fun ChatDetailsScreen(
    state: ChatDetailsState,
    action: (ChatDetailsAction) -> Unit
) {
    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    action(ChatDetailsAction.NavigateBack)
                                }
                                .padding(6.dp),
                            painter = painterResource(R.drawable.ic_arrow_left),
                            contentDescription = null,
                            tint = PrimaryColor,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = state.chat?.url.orEmpty(),
                            style = TextStyle(
                                fontSize = 16.sp,
                            ),
                            color = TextPrimaryColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryColor)
                            .height(1.dp),
                    )
                }
                RequestDetailsView(
                    chat = state.chat,
                    isVisible = state.showRequestDetails,
                    onOpen = {
                        action(ChatDetailsAction.ShowRequestDetail)
                    },
                    onClose = {
                        action(ChatDetailsAction.HideRequestDetail)
                    }
                )
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.chat?.messages.isNullOrEmpty().not()) {
                items(
                    items = state.chat.messages,
                    key = { message -> message.id }
                ) { message ->
                    if (message.isIncoming) {
                        ServerMessageView(message)
                    } else {
                        ClientMessageView(message)
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
private fun RequestDetailsView(
    chat: GiraffeChat?,
    isVisible: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
) {
    chat?.let { chat ->
        if (isVisible) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                AnimatedVisibility(
                    visible = true,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = BGSecondaryColor,
                                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        val textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = TextPrimaryColor,
                        )
                        Text(
                            text = "Url: ${chat.url}",
                            style = textStyle,
                        )
                        Text(
                            text = "Start time: ${chat.timestamp.timestampToDateTime()}",
                            style = textStyle,
                        )
                        if (chat.status != GiraffeChatStatus.InProgress) {
                            Text(
                                text = "End time: ${chat.messages.lastOrNull()?.timestamp?.timestampToDateTime()}",
                                style = textStyle
                            )
                        }
                        Text(
                            text = "Status: ${chat.status}",
                            style = textStyle,
                        )
                        if (chat.headers.isNotEmpty()) {
                            Text(
                                text = "Headers:",
                                style = textStyle,
                            )
                            chat.headers.forEach { header ->
                                Text(
                                    text = "${header.key}: ${header.value}",
                                    style = textStyle,
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                modifier = Modifier
                    .rotate(
                        animateFloatAsState(if (isVisible) 180f else 0f).value
                    )
                    .clip(CircleShape)
                    .clickable {
                        if (isVisible) onClose() else onOpen()
                    },
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = PrimaryColor,
            )
        }
    }
}

@Composable
private fun ServerMessageView(message: GiraffeMessage) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 40.dp,
            ),
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = BGSecondaryColor,
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp,
                    )
                )
                .padding(8.dp),
        ) {
            Text(
                text = message.textContent.orEmpty(),
                style = TextStyle(
                    fontSize = 14.sp,
                ),
                color = TextPrimaryColor,
            )
            if (message.contentType == GiraffeContentType.Image &&
                message.filePath.isNullOrBlank().not()
            ) {
                Spacer(Modifier.height(8.dp))

                val aspectRatio = remember(message.filePath) {
                    decodeImageAspectRatio(message.filePath)
                }
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val targetHeight = (maxWidth / (aspectRatio ?: 0f)).coerceIn(120.dp, 260.dp)

                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(targetHeight)
                            .clip(RoundedCornerShape(14.dp)),
                        model = File(message.filePath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.padding(start = 4.dp),
        ) {
            if (message.textContent.isNullOrBlank().not()) {
                Icon(
                    modifier = Modifier
                        .size(14.dp)
                        .clickable {
                            message.textContent.copyToClipboard(context)
                        },
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = null,
                    tint = TextPrimaryColor,
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = message.timestamp.timestampToTime(),
                style = TextStyle(fontSize = 12.sp),
                color = PrimaryColor
            )
        }

    }
}

@Composable
private fun ClientMessageView(message: GiraffeMessage) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 40.dp,
                end = 16.dp,
            ),
        horizontalAlignment = Alignment.End,
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = BGSecondaryColor,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp,
                    )
                )
                .padding(8.dp),
        ) {
            Text(
                text = message.textContent.orEmpty(),
                style = TextStyle(
                    fontSize = 14.sp,
                ),
                color = TextPrimaryColor,
            )
            if (message.contentType == GiraffeContentType.Image &&
                message.filePath.isNullOrBlank().not()
            ) {
                AsyncImage(
                    model = File(message.filePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Row(
            modifier = Modifier.padding(end = 4.dp),
        ) {
            Text(
                text = message.timestamp.timestampToTime(),
                style = TextStyle(fontSize = 12.sp),
                color = PrimaryColor
            )
            if (message.textContent.isNullOrBlank().not()) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    modifier = Modifier
                        .size(14.dp)
                        .clickable {
                            message.textContent.copyToClipboard(context)
                        },
                    painter = painterResource(R.drawable.ic_copy),
                    contentDescription = null,
                    tint = TextPrimaryColor,
                )
            }
        }
    }
}