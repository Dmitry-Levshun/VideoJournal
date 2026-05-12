package com.example.videojournal.di

import com.example.videojournal.FakeVideoFileRepository
import com.example.videojournal.FakeVideoJournalRepository
import com.example.videojournal.domain.repository.VideoFileRepository
import com.example.videojournal.domain.repository.VideoJournalRepository
import com.example.videojournal.presentation.feed.FeedViewModel
import com.example.videojournal.presentation.record.RecordViewModel
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.GlobalContext.stopKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get

class KoinModuleTest : KoinTest {
    @Test
    fun presentationGraph_createsViewModels() {
        val testModule = module {
            single(named("io")) { Dispatchers.Unconfined }
            single<VideoJournalRepository> { FakeVideoJournalRepository() }
            single<VideoFileRepository> { FakeVideoFileRepository() }
            factory { com.example.videojournal.domain.usecase.ObserveVideosUseCase(get()) }
            factory { com.example.videojournal.domain.usecase.SaveVideoUseCase(get()) }
            factory { com.example.videojournal.domain.usecase.CreateVideoCaptureTargetUseCase(get()) }
            factory { com.example.videojournal.domain.usecase.DiscardVideoCaptureTargetUseCase(get()) }
            viewModel { FeedViewModel(get()) }
            viewModel { RecordViewModel(get(), get(), get()) }
        }

        stopKoin()
        startKoin { modules(testModule) }
        try {
            assertNotNull(get<FeedViewModel>())
            assertNotNull(get<RecordViewModel>())
        } finally {
            stopKoin()
        }
    }
}
