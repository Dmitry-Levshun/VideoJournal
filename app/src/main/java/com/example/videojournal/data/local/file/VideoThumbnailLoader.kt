package com.example.videojournal.data.local.file

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import androidx.core.graphics.scale

private const val THUMBNAIL_CACHE_KB = 8 * 1024
private const val THUMBNAIL_MAX_SIZE_PX = 320

class VideoThumbnailLoader(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val cache = object : LruCache<String, Bitmap>(THUMBNAIL_CACHE_KB) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return (value.allocationByteCount / 1024).coerceAtLeast(1)
        }
    }

    suspend fun load(videoUri: String): Bitmap? {
        cache.get(videoUri)?.let { return it }

        return withContext(ioDispatcher) {
            cache.get(videoUri) ?: loadFrame(videoUri)?.also { bitmap ->
                cache.put(videoUri, bitmap)
            }
        }
    }

    private fun loadFrame(videoUri: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri.toUri())
            retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?.scaledForThumbnail()
        } catch (_: RuntimeException) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun Bitmap.scaledForThumbnail(): Bitmap {
        val largestSide = maxOf(width, height)
        if (largestSide <= THUMBNAIL_MAX_SIZE_PX) return this

        val scale = THUMBNAIL_MAX_SIZE_PX.toFloat() / largestSide
        val scaledWidth = (width * scale).toInt().coerceAtLeast(1)
        val scaledHeight = (height * scale).toInt().coerceAtLeast(1)
        val scaled = this.scale(scaledWidth, scaledHeight)
        if (scaled != this) recycle()
        return scaled
    }
}
