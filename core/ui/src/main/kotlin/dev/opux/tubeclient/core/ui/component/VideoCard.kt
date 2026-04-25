package dev.opux.tubeclient.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.opux.tubeclient.core.domain.model.VideoPreview
import dev.opux.tubeclient.core.ui.util.formatRelativeUploadDate
import dev.opux.tubeclient.core.ui.util.formatViewCount

@Composable
fun VideoCard(
    video: VideoPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VideoThumbnail(
            thumbnailUrl = video.thumbnailUrl,
            durationSeconds = video.durationSeconds,
            isLive = video.isLive,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
        )
        Text(
            text = video.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val meta = buildString {
            append(video.channelName)
            if (video.viewCount > 0) {
                append(" · ")
                append(video.viewCount.formatViewCount())
                append(" görüntülenme")
            }
            video.uploadedAt.formatRelativeUploadDate()?.let {
                append(" · ")
                append(it)
            }
        }
        Text(
            text = meta,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
