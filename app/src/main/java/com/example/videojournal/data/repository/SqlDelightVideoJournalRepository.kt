package com.example.videojournal.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.videojournal.data.local.db.VideoEntryQueries
import com.example.videojournal.domain.model.VideoEntry
import com.example.videojournal.domain.repository.VideoJournalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightVideoJournalRepository(
    private val queries: VideoEntryQueries,
    private val ioDispatcher: CoroutineDispatcher,
) : VideoJournalRepository {
    override fun observeVideos(): Flow<List<VideoEntry>> {
        return queries.selectLatest()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { rows ->
                rows.map { row ->
                    VideoEntry(
                        id = row.id,
                        videoUri = row.videoUri,
                        description = row.description,
                        createdAt = row.createdAt,
                    )
                }
            }
    }

    override suspend fun saveVideo(videoUri: String, description: String?, createdAt: Long) {
        withContext(ioDispatcher) {
            queries.insertVideo(
                videoUri = videoUri,
                description = description,
                createdAt = createdAt,
            )
        }
    }
}
