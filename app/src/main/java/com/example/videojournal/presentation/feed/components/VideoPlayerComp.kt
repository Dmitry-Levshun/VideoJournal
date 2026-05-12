package com.example.videojournal.presentation.feed.components

import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.videojournal.R
import com.example.videojournal.domain.model.VideoEntry
import com.example.videojournal.presentation.feed.FeedUiState
import com.example.videojournal.common.formatCreatedAt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged

private const val DURATION_FORMAT = "%d:%02d"
private const val PLAYER_POSITION_LABEL_FORMAT = "%d / %d"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerComp(
    state: FeedUiState,
    initialVideoId: Long?,
    contentPadding: PaddingValues,
    onFocusedVideoChanged: (Long?) -> Unit,
    onVideoTapped: (Long) -> Unit,
    onShareClicked: (VideoEntry) -> Unit,
    onBackClicked: () -> Unit,
) {
    if (state.videos.isEmpty()) {
        EmptyFeed(modifier = Modifier.padding(contentPadding))
        return
    }

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(initialVideoId, state.videos) {
        val initialIndex = state.videos.indexOfFirst { it.id == initialVideoId }.coerceAtLeast(0)
        listState.scrollToItem(initialIndex)
        onFocusedVideoChanged(state.videos.getOrNull(initialIndex)?.id)
    }

    LaunchedEffect(state.videos) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                onFocusedVideoChanged(state.videos.getOrNull(index)?.id)
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(contentPadding),
        state = listState,
        flingBehavior = flingBehavior,
    ) {
        itemsIndexed(
            items = state.videos,
            key = { _, video -> video.id },
        ) { index, video ->
            VideoPage(
                video = video,
                positionLabel = PLAYER_POSITION_LABEL_FORMAT.format(index + 1, state.videos.size),
                shouldPlay = state.focusedVideoId == video.id && state.isPlaying,
                onVideoTapped = onVideoTapped,
                onShareClicked = onShareClicked,
                onBackClicked = onBackClicked,
                modifier = Modifier.fillParentMaxSize(),
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun VideoPage(
    video: VideoEntry,
    positionLabel: String,
    shouldPlay: Boolean,
    onVideoTapped: (Long) -> Unit,
    onShareClicked: (VideoEntry) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var playerPositionMs by remember(video.videoUri) { mutableLongStateOf(0L) }
    var playerDurationMs by remember(video.videoUri) { mutableLongStateOf(0L) }
    val player = remember(video.videoUri) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            setMediaItem(MediaItem.fromUri(video.videoUri))
            prepare()
        }
    }

    LaunchedEffect(shouldPlay) {
        if (shouldPlay) player.play() else player.pause()
    }
    LaunchedEffect(player, shouldPlay) {
        if (!shouldPlay) {
            playerPositionMs = player.currentPosition.coerceAtLeast(0L)
            playerDurationMs = player.duration.takeIf { it > 0L } ?: 0L
            return@LaunchedEffect
        }

        while (true) {
            playerPositionMs = player.currentPosition.coerceAtLeast(0L)
            playerDurationMs = player.duration.takeIf { it > 0L } ?: 0L
            delay(250L)
        }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }

    Box(
        modifier = modifier
            .background(Color.Black)
            .clickable { onVideoTapped(video.id) },
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PlayerView(viewContext).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    this.player = player
                }
            },
            update = { it.player = player },
        )

        Text(
            text = positionLabel,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 18.dp, end = 18.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )

        TextButton(
            onClick = onBackClicked,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 10.dp, start = 10.dp),
        ) {
            Text(stringResource(R.string.back), color = Color.White)
        }

        AnimatedVisibility(
            visible = !shouldPlay,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.play), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.42f))
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text(
                text = video.description?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.untitled_clip),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = formatCreatedAt(
                        timestamp = video.createdAt,
                    ),
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = { onShareClicked(video) }) {
                    Text(stringResource(R.string.share), color = Color.White)
                }
            }
            Spacer(Modifier.height(8.dp))
            PlaybackSlider(
                positionMs = playerPositionMs,
                durationMs = playerDurationMs,
                onSeek = { positionMs -> player.seekTo(positionMs) },
            )
        }
    }
}

@Composable
private fun PlaybackSlider(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
) {
    Column {
        Slider(
            value = if (durationMs > 0L) positionMs.coerceIn(0L, durationMs).toFloat() else 0f,
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat(),
            enabled = durationMs > 0L,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDuration(positionMs),
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = formatDuration(durationMs),
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1_000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return DURATION_FORMAT.format(minutes, seconds)
}
