package com.example.videojournal

import com.example.videojournal.domain.model.VideoCaptureTarget
import com.example.videojournal.domain.model.VideoEntry
import com.example.videojournal.domain.repository.VideoFileRepository
import com.example.videojournal.domain.repository.VideoJournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeVideoJournalRepository : VideoJournalRepository {
    private var nextId = 1L
    private val videos = MutableStateFlow<List<VideoEntry>>(emptyList())

    override fun observeVideos(): Flow<List<VideoEntry>> = videos

    override suspend fun saveVideo(videoUri: String, description: String?, createdAt: Long) {
        videos.value = listOf(
            VideoEntry(
                id = nextId++,
                videoUri = videoUri,
                description = description,
                createdAt = createdAt,
            ),
        ) + videos.value
    }

    fun emit(items: List<VideoEntry>) {
        videos.value = items
    }
}

class FakeVideoFileRepository : VideoFileRepository {
    var discarded: VideoCaptureTarget? = null
    private val target = VideoCaptureTarget(
        contentUri = "content://test/video.mp4",
        filePath = "/tmp/video.mp4",
    )

    override fun createCaptureTarget(): VideoCaptureTarget = target

    override fun discard(target: VideoCaptureTarget) {
        discarded = target
    }
}
