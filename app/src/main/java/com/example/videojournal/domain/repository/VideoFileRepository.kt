package com.example.videojournal.domain.repository

import com.example.videojournal.domain.model.VideoCaptureTarget

interface VideoFileRepository {
    fun createCaptureTarget(): VideoCaptureTarget
    fun discard(target: VideoCaptureTarget)
}
