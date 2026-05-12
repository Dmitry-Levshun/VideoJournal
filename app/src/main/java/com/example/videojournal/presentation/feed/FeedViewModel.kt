package com.example.videojournal.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videojournal.domain.model.VideoEntry
import com.example.videojournal.domain.usecase.ObserveVideosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FeedUiState(
    val videos: List<VideoEntry> = emptyList(),
    val focusedVideoId: Long? = null,
    val isPlaying: Boolean = true,
    val inlineVideoId: Long? = null,
    val isInlinePlaying: Boolean = false,
)

class FeedViewModel(
    observeVideosUseCase: ObserveVideosUseCase,
) : ViewModel() {
    private val playbackState = MutableStateFlow(PlaybackState())
    private val inlinePlaybackState = MutableStateFlow(InlinePlaybackState())

    val uiState: StateFlow<FeedUiState> = combine(
        observeVideosUseCase(),
        playbackState,
        inlinePlaybackState,
    ) { videos, playback, inlinePlayback ->
        val focusedId = playback.focusedVideoId ?: videos.firstOrNull()?.id
        FeedUiState(
            videos = videos,
            focusedVideoId = focusedId,
            isPlaying = playback.isPlaying,
            inlineVideoId = inlinePlayback.videoId,
            isInlinePlaying = inlinePlayback.isPlaying,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FeedUiState(),
    )

    fun onFocusedVideoChanged(videoId: Long?) {
        playbackState.value = playbackState.value.copy(
            focusedVideoId = videoId,
            isPlaying = videoId != null,
        )
        if (videoId != null) {
            inlinePlaybackState.value = InlinePlaybackState()
        }
    }

    fun onVideoTapped(videoId: Long) {
        inlinePlaybackState.value = InlinePlaybackState()
        val current = playbackState.value
        val effectiveFocusedId = current.focusedVideoId ?: uiState.value.videos.firstOrNull()?.id
        playbackState.value = if (effectiveFocusedId == videoId) {
            current.copy(
                focusedVideoId = videoId,
                isPlaying = !current.isPlaying,
            )
        } else {
            PlaybackState(focusedVideoId = videoId, isPlaying = true)
        }
    }

    fun onInlineVideoTapped(videoId: Long) {
        playbackState.value = PlaybackState()
        val current = inlinePlaybackState.value
        inlinePlaybackState.value = if (current.videoId == videoId) {
            current.copy(isPlaying = !current.isPlaying)
        } else {
            InlinePlaybackState(videoId = videoId, isPlaying = true)
        }
    }

    private data class PlaybackState(
        val focusedVideoId: Long? = null,
        val isPlaying: Boolean = true,
    )

    private data class InlinePlaybackState(
        val videoId: Long? = null,
        val isPlaying: Boolean = false,
    )
}
