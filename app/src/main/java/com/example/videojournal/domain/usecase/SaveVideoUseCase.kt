package com.example.videojournal.domain.usecase

import com.example.videojournal.domain.repository.VideoJournalRepository

class SaveVideoUseCase(
    private val repository: VideoJournalRepository,
) {
    suspend operator fun invoke(videoUri: String, description: String?, createdAt: Long) {
        repository.saveVideo(
            videoUri = videoUri,
            description = description?.trim()?.takeIf { it.isNotEmpty() },
            createdAt = createdAt,
        )
    }
}
