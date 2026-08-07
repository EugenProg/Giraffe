package com.kogen.giraffe.ui.features.videoPreview.presentation.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.kogen.giraffe.R
import com.kogen.giraffe.ui.common.presentation.PreviewIconButton
import com.kogen.giraffe.ui.common.presentation.extensions.shareFile
import java.io.File

/**
 * Full-screen, auto-playing video preview, in the style of a chat app's video viewer: the video
 * fills the screen with the standard media3 playback controls, a back button and a share button
 * floating over the top edge. Unlike voice messages, video is never played inline in the chat -
 * tapping the thumbnail always lands here.
 */
@Composable
internal fun VideoPreviewScreen(
    filePath: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val file = remember(filePath) { File(filePath) }

    val exoPlayer = remember(filePath) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
            playWhenReady = true
            prepare()
        }
    }

    // The player owns native decoder resources - it must be released when this screen leaves
    // composition (back navigation), not just when the process dies.
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    player = exoPlayer
                }
            },
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PreviewIconButton(
                icon = R.drawable.ic_arrow_left,
                contentDescription = "Back",
                onClick = onBack,
            )
            PreviewIconButton(
                icon = R.drawable.ic_share,
                contentDescription = "Share",
                onClick = { file.shareFile(context, mimeType = "video/*") },
            )
        }
    }
}
