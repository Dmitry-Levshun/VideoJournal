package com.example.videojournal.presentation

import com.example.videojournal.FakeVideoJournalRepository
import com.example.videojournal.MainDispatcherRule
import com.example.videojournal.domain.model.VideoEntry
import com.example.videojournal.domain.usecase.ObserveVideosUseCase
import com.example.videojournal.presentation.feed.FeedViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FeedViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun uiState_usesFirstVideoAsFocusedByDefault() = runTest {
        val repository = FakeVideoJournalRepository()
        repository.emit(
            listOf(
                VideoEntry(2L, "content://two", null, 2L),
                VideoEntry(1L, "content://one", null, 1L),
            ),
        )

        val viewModel = FeedViewModel(ObserveVideosUseCase(repository))
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(2L, viewModel.uiState.value.focusedVideoId)
        assertTrue(viewModel.uiState.value.isPlaying)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tappingFocusedVideo_togglesPlayback() = runTest {
        val repository = FakeVideoJournalRepository()
        repository.emit(listOf(VideoEntry(1L, "content://one", null, 1L)))
        val viewModel = FeedViewModel(ObserveVideosUseCase(repository))
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onVideoTapped(1L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isPlaying)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun tappingInlineVideo_setsInlinePlayback() = runTest {
        val repository = FakeVideoJournalRepository()
        repository.emit(listOf(VideoEntry(1L, "content://one", null, 1L)))
        val viewModel = FeedViewModel(ObserveVideosUseCase(repository))
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onInlineVideoTapped(1L)
        advanceUntilIdle()

        assertEquals(1L, viewModel.uiState.value.inlineVideoId)
        assertTrue(viewModel.uiState.value.isInlinePlaying)

        viewModel.onInlineVideoTapped(1L)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isInlinePlaying)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun openingFullscreen_clearsInlinePlayback() = runTest {
        val repository = FakeVideoJournalRepository()
        repository.emit(listOf(VideoEntry(1L, "content://one", null, 1L)))
        val viewModel = FeedViewModel(ObserveVideosUseCase(repository))
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onInlineVideoTapped(1L)
        viewModel.onFocusedVideoChanged(1L)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.inlineVideoId)
        assertFalse(viewModel.uiState.value.isInlinePlaying)
    }
}
