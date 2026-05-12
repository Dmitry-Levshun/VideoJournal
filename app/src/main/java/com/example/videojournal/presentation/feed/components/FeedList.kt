package com.example.videojournal.presentation.feed.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.videojournal.R
import com.example.videojournal.common.formatCreatedAt
import com.example.videojournal.data.local.file.VideoThumbnailLoader
import com.example.videojournal.domain.model.VideoEntry
import com.example.videojournal.presentation.feed.FeedUiState
import org.koin.compose.koinInject

@Composable
fun FeedList(
    state: FeedUiState,
    contentPadding: PaddingValues,
    onVideoClicked: (VideoEntry) -> Unit,
    onShareClicked: (VideoEntry) -> Unit,
    thumbnailLoader: VideoThumbnailLoader = koinInject(),
) {
    if (state.videos.isEmpty()) {
        EmptyFeed(modifier = Modifier.padding(contentPadding))
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding),
        contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 6.dp)) {
                Text(
                    text = stringResource(R.string.feed_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = pluralStringResource(
                        id = R.plurals.local_clip_count,
                        count = state.videos.size,
                        state.videos.size,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f),
                )
            }
        }
        itemsIndexed(
            items = state.videos,
            key = { _, video -> video.id },
        ) { _, video ->
            VideoListItem(
                video = video,
                onClick = { onVideoClicked(video) },
                onShareClicked = { onShareClicked(video) },
                thumbnailLoader = thumbnailLoader,
            )
        }
    }
}

@Composable
private fun VideoListItem(
    video: VideoEntry,
    onClick: () -> Unit,
    onShareClicked: () -> Unit,
    thumbnailLoader: VideoThumbnailLoader,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                VideoThumbnail(
                    videoUri = video.videoUri,
                    thumbnailLoader = thumbnailLoader,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp),
            ) {
                Text(
                    text = video.description?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.untitled_clip),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = formatCreatedAt(
                        timestamp = video.createdAt,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onShareClicked) {
                    Text(stringResource(R.string.share))
                }
            }
        }
    }
}

@Composable
fun VideoThumbnail(
    videoUri: String,
    thumbnailLoader: VideoThumbnailLoader,
    modifier: Modifier = Modifier,
) {
    var thumbnail by remember(videoUri) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(videoUri) { mutableStateOf(false) }

    LaunchedEffect(videoUri) {
        failed = false
        thumbnail = thumbnailLoader.load(videoUri)
        failed = thumbnail == null
    }

    when {
        thumbnail != null -> {
            Image(
                bitmap = thumbnail!!.asImageBitmap(),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop,
            )
        }

        failed -> {
            Text(stringResource(R.string.play), color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        else -> {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        }
    }
}

