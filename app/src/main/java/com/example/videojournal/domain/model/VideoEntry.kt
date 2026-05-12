package com.example.videojournal.domain.model

data class VideoEntry(
    val id: Long,
    val videoUri: String,
    val description: String?,
    val createdAt: Long,
)
