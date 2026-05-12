package com.example.videojournal.data

import com.example.videojournal.FakeVideoJournalRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeRepositoryTest {
    @Test
    fun saveVideo_addsNewestItemFirst() = runTest {
        val repository = FakeVideoJournalRepository()

        repository.saveVideo("content://first", "First", 1L)
        repository.saveVideo("content://second", "Second", 2L)

        val videos = repository.observeVideos().first()
        assertEquals(listOf("content://second", "content://first"), videos.map { it.videoUri })
    }
}
