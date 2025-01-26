package com.example.photocatalog.ui.photos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photocatalog.data.models.Photo
import com.example.photocatalog.data.models.PhotoUpload
import com.example.photocatalog.data.source.PhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val photosRepository: PhotosRepository
) : ViewModel() {

    private val _photoState = MutableStateFlow<PhotoState>(PhotoState.Loading)
    val photoState: StateFlow<PhotoState> = _photoState

    private val _uploadState = MutableLiveData<UploadState>()
    val uploadState: LiveData<UploadState> = _uploadState

    fun loadPhotos(projectId: String? = null) {
        viewModelScope.launch {
            try {
                _photoState.value = PhotoState.Loading
                val photos = photosRepository.getPhotos(projectId)
                _photoState.value = PhotoState.Success(photos)
            } catch (e: Exception) {
                _photoState.value = PhotoState.Error(e.message ?: "Failed to load photos")
            }
        }
    }

    fun uploadPhoto(photoUpload: PhotoUpload) {
        viewModelScope.launch {
            try {
                _uploadState.value = UploadState.Uploading
                val photo = photosRepository.uploadPhoto(photoUpload)
                _uploadState.value = UploadState.Success(photo)
                loadPhotos(photoUpload.projectId)
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "Upload failed")
            }
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            try {
                photosRepository.deletePhoto(photoId)
                loadPhotos() // Reload the photos list
            } catch (e: Exception) {
                _photoState.value = PhotoState.Error(e.message ?: "Failed to delete photo")
            }
        }
    }

    fun updatePhotoMetadata(photoId: String, metadata: Map<String, Any>) {
        viewModelScope.launch {
            try {
                photosRepository.updatePhotoMetadata(photoId, metadata)
                loadPhotos() // Reload to show updated metadata
            } catch (e: Exception) {
                _photoState.value = PhotoState.Error(e.message ?: "Failed to update photo")
            }
        }
    }
}

sealed class PhotoState {
    object Loading : PhotoState()
    data class Success(val photos: List<Photo>) : PhotoState()
    data class Error(val message: String) : PhotoState()
}

sealed class UploadState {
    object Uploading : UploadState()
    data class Success(val photo: Photo) : UploadState()
    data class Error(val message: String) : UploadState()
}
