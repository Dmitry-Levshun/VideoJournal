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
)

class FeedViewModel(
    observeVideosUseCase: ObserveVideosUseCase,
) : ViewModel() {
    private val playbackState = MutableStateFlow(PlaybackState())

    val uiState: StateFlow<FeedUiState> = combine(
        observeVideosUseCase(),
        playbackState,
    ) { videos, playback ->
        val focusedId = playback.focusedVideoId ?: videos.firstOrNull()?.id
        FeedUiState(
            videos = videos,
            focusedVideoId = focusedId,
            isPlaying = playback.isPlaying,
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
    }

    fun onVideoTapped(videoId: Long) {
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

    private data class PlaybackState(
        val focusedVideoId: Long? = null,
        val isPlaying: Boolean = true,
    )
}
