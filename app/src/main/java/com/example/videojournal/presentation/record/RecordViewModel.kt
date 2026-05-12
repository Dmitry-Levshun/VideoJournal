package com.example.videojournal.presentation.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videojournal.domain.model.VideoCaptureTarget
import com.example.videojournal.domain.usecase.CreateVideoCaptureTargetUseCase
import com.example.videojournal.domain.usecase.DiscardVideoCaptureTargetUseCase
import com.example.videojournal.domain.usecase.SaveVideoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordUiState(
    val pendingCaptureTarget: VideoCaptureTarget? = null,
    val showDescriptionDialog: Boolean = false,
    val permissionDenied: Boolean = false,
    val error: RecordError? = null,
)

enum class RecordError {
    CameraPermissionRequired,
    CameraUnavailable,
    VideoFileCreateError,
}

class RecordViewModel(
    private val createVideoCaptureTargetUseCase: CreateVideoCaptureTargetUseCase,
    private val discardVideoCaptureTargetUseCase: DiscardVideoCaptureTargetUseCase,
    private val saveVideoUseCase: SaveVideoUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    fun onPermissionDenied() {
        _uiState.update {
            it.copy(
                permissionDenied = true,
                error = RecordError.CameraPermissionRequired,
            )
        }
    }

    fun onCameraUnavailable() {
        _uiState.update {
            it.copy(error = RecordError.CameraUnavailable)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(error = null) }
    }

    fun prepareCapture(): VideoCaptureTarget? {
        return runCatching { createVideoCaptureTargetUseCase() }
            .onSuccess { target ->
                _uiState.update {
                    it.copy(
                        pendingCaptureTarget = target,
                        permissionDenied = false,
                        error = null,
                    )
                }
            }
            .onFailure {
                _uiState.update { state ->
                    state.copy(error = RecordError.VideoFileCreateError)
                }
            }
            .getOrNull()
    }

    fun onCaptureResult(success: Boolean) {
        val target = _uiState.value.pendingCaptureTarget ?: return
        if (success) {
            _uiState.update { it.copy(showDescriptionDialog = true, error = null) }
        } else {
            discardVideoCaptureTargetUseCase(target)
            _uiState.update {
                it.copy(
                    pendingCaptureTarget = null,
                    showDescriptionDialog = false,
                )
            }
        }
    }

    fun save(description: String) {
        val target = _uiState.value.pendingCaptureTarget ?: return
        viewModelScope.launch {
            saveVideoUseCase(
                videoUri = target.contentUri,
                description = description,
                createdAt = System.currentTimeMillis(),
            )
            _uiState.update {
                it.copy(
                    pendingCaptureTarget = null,
                    showDescriptionDialog = false,
                    error = null,
                )
            }
        }
    }

    fun dismissDescription() {
        _uiState.value.pendingCaptureTarget?.let(discardVideoCaptureTargetUseCase::invoke)
        _uiState.update {
            it.copy(
                pendingCaptureTarget = null,
                showDescriptionDialog = false,
            )
        }
    }
}
