package com.example.videojournal.common

import android.text.format.DateFormat

private const val CREATED_AT_FORMAT = "MMM d, yyyy HH:mm"

fun formatCreatedAt(timestamp: Long): String {
    return DateFormat.format(CREATED_AT_FORMAT, timestamp).toString()
}