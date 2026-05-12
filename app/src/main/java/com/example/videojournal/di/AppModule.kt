package com.example.videojournal.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.videojournal.data.local.db.VideoJournalDatabase
import com.example.videojournal.data.local.file.AppVideoFileRepository
import com.example.videojournal.data.local.file.VideoThumbnailLoader
import com.example.videojournal.data.repository.SqlDelightVideoJournalRepository
import com.example.videojournal.domain.repository.VideoFileRepository
import com.example.videojournal.domain.repository.VideoJournalRepository
import com.example.videojournal.domain.usecase.CreateVideoCaptureTargetUseCase
import com.example.videojournal.domain.usecase.DiscardVideoCaptureTargetUseCase
import com.example.videojournal.domain.usecase.ObserveVideosUseCase
import com.example.videojournal.domain.usecase.SaveVideoUseCase
import com.example.videojournal.presentation.feed.FeedViewModel
import com.example.videojournal.presentation.record.RecordViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {
    single(named("io")) { Dispatchers.IO }
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = VideoJournalDatabase.Schema,
            context = androidContext(),
            name = "video_journal.db",
        )
    }
    single { VideoJournalDatabase(get()) }
    single { get<VideoJournalDatabase>().videoEntryQueries }
    single<VideoJournalRepository> {
        SqlDelightVideoJournalRepository(
            queries = get(),
            ioDispatcher = get(named("io")),
        )
    }
    single<VideoFileRepository> { AppVideoFileRepository(androidContext()) }
    single { VideoThumbnailLoader(androidContext(), get(named("io"))) }
    factory { ObserveVideosUseCase(get()) }
    factory { SaveVideoUseCase(get()) }
    factory { CreateVideoCaptureTargetUseCase(get()) }
    factory { DiscardVideoCaptureTargetUseCase(get()) }
    viewModel { FeedViewModel(get()) }
    viewModel { RecordViewModel(get(), get(), get()) }
}
