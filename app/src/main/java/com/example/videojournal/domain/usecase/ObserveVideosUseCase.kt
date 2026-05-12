package com.example.videojournal.domain.usecase

import com.example.videojournal.domain.repository.VideoJournalRepository

class ObserveVideosUseCase(
    private val repository: VideoJournalRepository,
) {
    operator fun invoke() = repository.observeVideos()
}
