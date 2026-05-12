package com.example.videojournal.presentation.feed

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.videojournal.R
import com.example.videojournal.domain.model.VideoEntry
import com.example.videojournal.presentation.feed.components.FeedList
import com.example.videojournal.presentation.feed.components.VideoPlayerComp
import com.example.videojournal.presentation.record.RecordError
import com.example.videojournal.presentation.record.RecordViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val VIDEO_MIME_TYPE = "video/mp4"

@Composable
fun FeedScreen(
    feedViewModel: FeedViewModel = koinViewModel(),
    recordViewModel: RecordViewModel = koinViewModel(),
) {
    val feedState by feedViewModel.uiState.collectAsState()
    val recordState by recordViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedVideoId by remember { mutableStateOf<Long?>(null) }

    val captureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
    ) { success ->
        recordViewModel.onCaptureResult(success)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            recordViewModel.prepareCapture()?.let { target ->
                captureLauncher.launch(target.contentUri.toUri())
            }
        } else {
            recordViewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(recordState.error) {
        val error = recordState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(context.getString(error.messageResId))
        recordViewModel.clearMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (selectedVideoId == null) {
                FloatingActionButton(
                    onClick = {
                        val hasCamera = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                        if (!hasCamera) {
                            recordViewModel.onCameraUnavailable()
                            return@FloatingActionButton
                        }
                        val captureIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                        if (captureIntent.resolveActivity(context.packageManager) == null) {
                            recordViewModel.onCameraUnavailable()
                            return@FloatingActionButton
                        }
                        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permission == PackageManager.PERMISSION_GRANTED) {
                            recordViewModel.prepareCapture()?.let { target ->
                                captureLauncher.launch(target.contentUri.toUri())
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    shape = CircleShape,
                ) {
                    Text(stringResource(R.string.record_button), fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { padding ->
        val shareVideo: (VideoEntry) -> Unit = { video ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = VIDEO_MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, video.videoUri.toUri())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (shareIntent.resolveActivity(context.packageManager) == null) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.share_unavailable))
                }
            } else {
                val chooser = Intent.createChooser(shareIntent, context.getString(R.string.share_chooser_title))
                runCatching { context.startActivity(chooser) }
                    .onFailure {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(context.getString(R.string.share_unavailable))
                        }
                    }
            }
        }

        if (selectedVideoId == null) {
            FeedList(
                state = feedState,
                contentPadding = padding,
                onVideoClicked = { video ->
                    selectedVideoId = video.id
                    feedViewModel.onFocusedVideoChanged(video.id)
                },
                onShareClicked = shareVideo,
            )
        } else {
            BackHandler {
                selectedVideoId = null
                feedViewModel.onFocusedVideoChanged(null)
            }
            VideoPlayerComp(
                state = feedState,
                initialVideoId = selectedVideoId,
                contentPadding = padding,
                onFocusedVideoChanged = feedViewModel::onFocusedVideoChanged,
                onVideoTapped = feedViewModel::onVideoTapped,
                onShareClicked = shareVideo,
                onBackClicked = {
                    selectedVideoId = null
                    feedViewModel.onFocusedVideoChanged(null)
                },
            )
        }
    }

    if (recordState.showDescriptionDialog) {
        DescriptionDialog(
            onSave = recordViewModel::save,
            onDismiss = recordViewModel::dismissDescription,
        )
    }
}

@Composable
private fun DescriptionDialog(
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_description_title)) },
        text = {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                placeholder = { Text(stringResource(R.string.optional_note_placeholder)) },
            )
        },
        confirmButton = {
            Button(onClick = { onSave(description) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.discard))
            }
        },
        shape = RoundedCornerShape(8.dp),
    )
}

private val RecordError.messageResId: Int
    get() = when (this) {
        RecordError.CameraPermissionRequired -> R.string.camera_permission_required
        RecordError.CameraUnavailable -> R.string.camera_unavailable
        RecordError.VideoFileCreateError -> R.string.video_file_create_error
    }

