package com.example.videojournal.domain.repository

import com.example.videojournal.domain.model.VideoEntry
import kotlinx.coroutines.flow.Flow

interface VideoJournalRepository {
    fun observeVideos(): Flow<List<VideoEntry>>
    suspend fun saveVideo(videoUri: String, description: String?, createdAt: Long)
}
