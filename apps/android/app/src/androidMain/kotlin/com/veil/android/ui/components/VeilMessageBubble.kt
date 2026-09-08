package com.veil.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.veil.android.ui.theme.VeilBubbleShapeIncoming
import com.veil.android.ui.theme.VeilBubbleShapeOutgoing
import com.veil.android.ui.theme.VeilDarkIncomingBubble
import com.veil.android.ui.theme.VeilDarkOnIncomingBubble
import com.veil.android.ui.theme.VeilDarkOnOutgoingBubble
import com.veil.android.ui.theme.VeilDarkOutgoingBubble
import com.veil.android.ui.theme.VeilLightIncomingBubble
import com.veil.android.ui.theme.VeilLightOnIncomingBubble
import com.veil.android.ui.theme.VeilLightOnOutgoingBubble
import com.veil.android.ui.theme.VeilLightOutgoingBubble
import com.veil.android.ui.theme.VeilSpacing
import com.veil.android.ui.theme.isVeilDarkTheme
import com.veil.shared.domain.model.ChatMessage
import com.veil.shared.domain.model.MessageStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VeilMessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    val isDark = isVeilDarkTheme()
    val isOutgoing = message.isOutgoing
    val alignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor =
        if (isOutgoing) {
            if (isDark) VeilDarkOutgoingBubble else VeilLightOutgoingBubble
        } else {
            if (isDark) VeilDarkIncomingBubble else VeilLightIncomingBubble
        }
    val textColor =
        if (isOutgoing) {
            if (isDark) VeilDarkOnOutgoingBubble else VeilLightOnOutgoingBubble
        } else {
            if (isDark) VeilDarkOnIncomingBubble else VeilLightOnIncomingBubble
        }
    val shape = if (isOutgoing) VeilBubbleShapeOutgoing else VeilBubbleShapeIncoming
    val maxWidth = LocalConfiguration.current.screenWidthDp.dp * 0.78f

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = maxWidth)
                    .clip(shape)
                    .background(bubbleColor)
                    .padding(
                        horizontal = VeilSpacing.bubblePaddingHorizontal,
                        vertical = VeilSpacing.bubblePaddingVertical,
                    ),
        ) {
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
            )
            Row(
                modifier = Modifier.align(Alignment.End).padding(top = VeilSpacing.xxs),
                horizontalArrangement = Arrangement.spacedBy(VeilSpacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatMessageTime(message.sentAtEpochMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.65f),
                )
                if (isOutgoing) {
                    MessageStatusIcon(status = message.status, tint = textColor.copy(alpha = 0.75f))
                }
            }
        }
    }
}

@Composable
private fun MessageStatusIcon(
    status: MessageStatus,
    tint: androidx.compose.ui.graphics.Color,
) {
    val icon =
        when (status) {
            MessageStatus.PENDING -> Icons.Default.Schedule
            MessageStatus.SENT -> Icons.Default.Check
            MessageStatus.DELIVERED -> Icons.Default.DoneAll
            MessageStatus.FAILED -> Icons.Default.Error
        }
    Icon(
        imageVector = icon,
        contentDescription = status.name,
        modifier = Modifier.size(14.dp),
        tint = if (status == MessageStatus.FAILED) MaterialTheme.colorScheme.error else tint,
    )
}

private fun formatMessageTime(epochMs: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - epochMs
    return when {
        diff < 60_000 -> "Now"
        diff < 86_400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
        diff < 604_800_000 -> SimpleDateFormat("EEE HH:mm", Locale.getDefault()).format(Date(epochMs))
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMs))
    }
}

fun formatConversationTime(epochMs: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - epochMs
    return when {
        diff < 60_000 -> "Now"
        diff < 86_400_000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
        diff < 604_800_000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(epochMs))
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochMs))
    }
}
