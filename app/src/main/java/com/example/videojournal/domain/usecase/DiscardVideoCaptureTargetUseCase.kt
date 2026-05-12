package com.example.videojournal.domain.usecase

import com.example.videojournal.domain.model.VideoCaptureTarget
import com.example.videojournal.domain.repository.VideoFileRepository

class DiscardVideoCaptureTargetUseCase(
    private val repository: VideoFileRepository,
) {
    operator fun invoke(target: VideoCaptureTarget) = repository.discard(target)
}
