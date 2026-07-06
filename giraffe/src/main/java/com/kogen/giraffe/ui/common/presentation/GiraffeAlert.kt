package com.kogen.giraffe.ui.common.presentation

import android.view.Gravity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.kogen.giraffe.ui.common.main.Background
import com.kogen.giraffe.ui.common.main.TextPrimary

@Composable
fun GiraffeAlert(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    confirmButton: GiraffeButtonData,
    cancelButton: GiraffeButtonData,
    closeClicked: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(24.dp)

    Dialog(
        onDismissRequest = {
            closeClicked?.invoke()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true,
        )
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        dialogWindowProvider.window.setGravity(Gravity.CENTER)

        Column(
            modifier = modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                )
                .fillMaxWidth()
                .background(
                    color = Background,
                    shape = shape
                )
                .clip(shape)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                style = TextStyle(
                    fontSize = 24.sp,
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )

            description?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    style = TextStyle(
                        fontSize = 16.sp,
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                GiraffeButton(
                    modifier = Modifier.weight(1f),
                    title = confirmButton.title,
                    style = confirmButton.style,
                    onClick = confirmButton.onClick,
                )
                Spacer(Modifier.width(8.dp))
                GiraffeButton(
                    modifier = Modifier.weight(1f),
                    title = cancelButton.title,
                    style = cancelButton.style,
                    onClick = cancelButton.onClick,
                )
            }
        }
    }
}