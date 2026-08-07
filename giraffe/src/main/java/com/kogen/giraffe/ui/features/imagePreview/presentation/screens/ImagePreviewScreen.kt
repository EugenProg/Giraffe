package com.kogen.giraffe.ui.features.imagePreview.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kogen.giraffe.R
import com.kogen.giraffe.ui.common.presentation.PreviewIconButton
import com.kogen.giraffe.ui.common.presentation.extensions.shareFile
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import java.io.File

/**
 * Full-screen, pinch-to-zoom preview of a single image, in the style of a chat app's photo
 * viewer: black backdrop, the image fit to the screen and zoomable/pannable, a back button and a
 * share button floating over the top edge.
 */
@Composable
internal fun ImagePreviewScreen(
    filePath: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val file = remember(filePath) { File(filePath) }
    val zoomState = rememberZoomState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        AsyncImage(
            model = file,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .zoomable(zoomState),
            onSuccess = { state ->
                zoomState.setContentSize(state.painter.intrinsicSize)
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
                onClick = { file.shareFile(context, mimeType = "image/*") },
            )
        }
    }
}
