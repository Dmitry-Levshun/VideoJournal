package com.example.videojournal.presentation

import com.example.videojournal.FakeVideoFileRepository
import com.example.videojournal.FakeVideoJournalRepository
import com.example.videojournal.MainDispatcherRule
import com.example.videojournal.domain.usecase.CreateVideoCaptureTargetUseCase
import com.example.videojournal.domain.usecase.DiscardVideoCaptureTargetUseCase
import com.example.videojournal.domain.usecase.SaveVideoUseCase
import com.example.videojournal.presentation.record.RecordViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RecordViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun prepareCapture_setsPendingTarget() {
        val viewModel = createViewModel()

        val target = viewModel.prepareCapture()

        assertNotNull(target)
        assertEquals("content://test/video.mp4", viewModel.uiState.value.pendingCaptureTarget?.contentUri)
    }

    @Test
    fun cancelledCapture_discardsPendingFile() {
        val fileRepository = FakeVideoFileRepository()
        val viewModel = createViewModel(fileRepository = fileRepository)

        val target = viewModel.prepareCapture()
        viewModel.onCaptureResult(success = false)

        assertEquals(target, fileRepository.discarded)
        assertNull(viewModel.uiState.value.pendingCaptureTarget)
    }

    @Test
    fun successfulCapture_thenSave_persistsVideo() = runTest {
        val journalRepository = FakeVideoJournalRepository()
        val viewModel = createViewModel(journalRepository = journalRepository)

        viewModel.prepareCapture()
        viewModel.onCaptureResult(success = true)
        assertTrue(viewModel.uiState.value.showDescriptionDialog)

        viewModel.save("Lunch walk")

        val saved = journalRepository.observeVideos().first().single()
        assertEquals("content://test/video.mp4", saved.videoUri)
        assertEquals("Lunch walk", saved.description)
        assertFalse(viewModel.uiState.value.showDescriptionDialog)
    }

    private fun createViewModel(
        fileRepository: FakeVideoFileRepository = FakeVideoFileRepository(),
        journalRepository: FakeVideoJournalRepository = FakeVideoJournalRepository(),
    ): RecordViewModel {
        return RecordViewModel(
            createVideoCaptureTargetUseCase = CreateVideoCaptureTargetUseCase(fileRepository),
            discardVideoCaptureTargetUseCase = DiscardVideoCaptureTargetUseCase(fileRepository),
            saveVideoUseCase = SaveVideoUseCase(journalRepository),
        )
    }
}
