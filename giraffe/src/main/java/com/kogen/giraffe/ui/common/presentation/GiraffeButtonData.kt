package com.kogen.giraffe.ui.common.presentation

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kogen.giraffe.ui.common.main.BGSecondary
import com.kogen.giraffe.ui.common.main.Error
import com.kogen.giraffe.ui.common.main.Primary
import com.kogen.giraffe.ui.common.main.TextPrimary

@Composable
fun GiraffeButton(
    modifier: Modifier = Modifier,
    title: String,
    style: GiraffeButtonStyle,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        colors = ButtonColors(
            containerColor = style.backgroundColor,
            contentColor = style.textColor,
            disabledContainerColor = style.backgroundColor,
            disabledContentColor = style.textColor,
        ),
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        elevation = null,
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
            ),
            color = style.textColor,
        )
    }
}

data class GiraffeButtonData(
    val title: String,
    val style: GiraffeButtonStyle,
    val onClick: () -> Unit,
)

enum class GiraffeButtonStyle(val backgroundColor: Color, val textColor: Color) {
    PrimaryType(Primary, TextPrimary),
    SecondaryType(BGSecondary, TextPrimary),
    NegativeType(Error, TextPrimary),
}