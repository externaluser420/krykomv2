package com.veil.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.veil.android.ui.theme.VeilSpacing
import com.veil.android.ui.theme.VeilUnread
import com.veil.shared.domain.model.Contact

data class ConversationPreview(
    val contact: Contact,
    val lastMessage: String?,
    val lastMessageTime: Long?,
    val unreadCount: Int = 0,
)

@Composable
fun VeilConversationRow(
    preview: ConversationPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contact = preview.contact
    val displayName = contact.displayName ?: contact.identityId.value.take(8)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = VeilSpacing.screenHorizontal, vertical = VeilSpacing.listItemVertical),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VeilAvatar(
            name = displayName,
            size = 52.dp,
            verified = contact.verified,
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = VeilSpacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (contact.verified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            modifier = Modifier.padding(start = VeilSpacing.xs).size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                preview.lastMessageTime?.let { time ->
                    Text(
                        text = formatConversationTime(time),
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            if (preview.unreadCount > 0) {
                                VeilUnread
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = VeilSpacing.xxs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = preview.lastMessage ?: "Start a conversation",
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (preview.unreadCount > 0) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (preview.unreadCount > 0) {
                    UnreadBadge(count = preview.unreadCount)
                }
            }
        }
    }
}

@Composable
fun VeilContactRow(
    contact: Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val displayName = contact.displayName ?: contact.identityId.value.take(8)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = VeilSpacing.screenHorizontal, vertical = VeilSpacing.listItemVertical),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VeilAvatar(name = displayName, size = 48.dp, verified = contact.verified)
        Column(
            modifier = Modifier.padding(start = VeilSpacing.md).weight(1f),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (contact.verified) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        modifier = Modifier.padding(start = VeilSpacing.xs).size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = subtitle ?: contact.identityId.value.take(16) + "…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    Text(
        text = if (count > 99) "99+" else count.toString(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier =
            Modifier
                .padding(start = VeilSpacing.sm)
                .background(VeilUnread, MaterialTheme.shapes.small)
                .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
fun VeilListDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.padding(start = 80.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp,
    )
}
