package com.veil.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.veil.android.ui.theme.VeilAvatarColors

@Composable
fun VeilAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    verified: Boolean = false,
) {
    val initials =
        remember(name) {
            name
                .trim()
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercaseChar().toString() }
                .ifEmpty { name.take(1).uppercase() }
        }
    val colorIndex = remember(name) { name.hashCode().mod(VeilAvatarColors.size).let { if (it < 0) -it else it } }
    val bgColor = VeilAvatarColors[colorIndex]

    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                ),
        )
    }
}
