package com.example.photocatalog.ui.camera

import android.net.Uri
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photocatalog.data.models.PhotoUpload
import com.example.photocatalog.data.source.CameraService
import com.example.photocatalog.data.source.PhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraService: CameraService,
    private val photosRepository: PhotosRepository
) : ViewModel() {

    private val _cameraState = MutableStateFlow<CameraState>(CameraState.Initial)
    val cameraState: StateFlow<CameraState> = _cameraState

    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        viewModelScope.launch {
            try {
                _cameraState.value = CameraState.Starting
                cameraService.startCamera(lifecycleOwner, previewView)
                _cameraState.value = CameraState.Preview
            } catch (e: Exception) {
                _cameraState.value = CameraState.Error(e.message ?: "Failed to start camera")
            }
        }
    }

    fun capturePhoto() {
        viewModelScope.launch {
            try {
                _cameraState.value = CameraState.Capturing
                val (file, uri) = cameraService.capturePhoto()
                _cameraState.value = CameraState.PhotoCaptured(file, uri)
            } catch (e: Exception) {
                _cameraState.value = CameraState.Error(e.message ?: "Failed to capture photo")
            }
        }
    }

    fun uploadPhoto(photoUpload: PhotoUpload) {
        viewModelScope.launch {
            try {
                _cameraState.value = CameraState.Uploading
                val photo = photosRepository.uploadPhoto(photoUpload)
                _cameraState.value = CameraState.Uploaded(photo)
            } catch (e: Exception) {
                _cameraState.value = CameraState.Error(e.message ?: "Failed to upload photo")
            }
        }
    }

    fun switchCamera() {
        cameraService.switchCamera()
        // Restart camera preview with new lens facing
        _cameraState.value.let { currentState ->
            if (currentState is CameraState.Preview) {
                _cameraState.value = CameraState.SwitchingCamera
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cameraService.release()
    }
}

sealed class CameraState {
    object Initial : CameraState()
    object Starting : CameraState()
    object Preview : CameraState()
    object Capturing : CameraState()
    object SwitchingCamera : CameraState()
    object Uploading : CameraState()
    data class PhotoCaptured(val file: File, val uri: Uri) : CameraState()
    data class Uploaded(val photo: com.example.photocatalog.data.models.Photo) : CameraState()
    data class Error(val message: String) : CameraState()
}
