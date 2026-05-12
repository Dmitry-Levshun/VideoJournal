package com.example.videojournal.domain

import com.example.videojournal.FakeVideoJournalRepository
import com.example.videojournal.domain.usecase.ObserveVideosUseCase
import com.example.videojournal.domain.usecase.SaveVideoUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UseCaseTest {
    @Test
    fun saveVideo_trimsBlankDescriptionToNull() = runTest {
        val repository = FakeVideoJournalRepository()
        val saveVideo = SaveVideoUseCase(repository)

        saveVideo(
            videoUri = "content://video",
            description = "   ",
            createdAt = 100L,
        )

        val saved = ObserveVideosUseCase(repository)().first().single()
        assertEquals("content://video", saved.videoUri)
        assertNull(saved.description)
        assertEquals(100L, saved.createdAt)
    }
}
