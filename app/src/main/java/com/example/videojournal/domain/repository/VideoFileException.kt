package com.example.videojournal.domain.repository

class VideoFileException(
    val reason: Reason,
    cause: Throwable? = null,
) : RuntimeException(reason.name, cause) {
    enum class Reason {
        StorageUnavailable,
        DirectoryCreateFailed,
        FileProviderFailed,
    }
}
