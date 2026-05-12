package com.example.videojournal.data.local.file

import android.content.Context
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.videojournal.domain.model.VideoCaptureTarget
import com.example.videojournal.domain.repository.VideoFileException
import com.example.videojournal.domain.repository.VideoFileRepository
import java.io.File

class AppVideoFileRepository(
    private val context: Context,
) : VideoFileRepository {
    override fun createCaptureTarget(): VideoCaptureTarget {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
            ?: throw VideoFileException(VideoFileException.Reason.StorageUnavailable)
        if (!directory.exists() && !directory.mkdirs()) {
            throw VideoFileException(VideoFileException.Reason.DirectoryCreateFailed)
        }
        val file = File(directory, "journal_${System.currentTimeMillis()}.mp4")
        val uri = runCatching {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
        }.getOrElse { error ->
            throw VideoFileException(VideoFileException.Reason.FileProviderFailed, error)
        }
        return VideoCaptureTarget(
            contentUri = uri.toString(),
            filePath = file.absolutePath,
        )
    }

    override fun discard(target: VideoCaptureTarget) {
        File(target.filePath).delete()
    }
}
