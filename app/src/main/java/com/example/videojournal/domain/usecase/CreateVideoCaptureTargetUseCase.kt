package com.example.videojournal.domain.usecase

import com.example.videojournal.domain.repository.VideoFileRepository

class CreateVideoCaptureTargetUseCase(
    private val repository: VideoFileRepository,
) {
    operator fun invoke() = repository.createCaptureTarget()
}
