package com.example.photocatalog.ui.photodetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photocatalog.data.models.Photo
import com.example.photocatalog.data.source.PhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    private val photosRepository: PhotosRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val photoId: String = checkNotNull(savedStateHandle["photoId"])
    
    private val _uiState = MutableStateFlow<PhotoDetailState>(PhotoDetailState.Loading)
    val uiState: StateFlow<PhotoDetailState> = _uiState

    init {
        loadPhoto()
    }

    private fun loadPhoto() {
        viewModelScope.launch {
            try {
                val photo = photosRepository.getPhoto(photoId)
                if (photo != null) {
                    _uiState.value = PhotoDetailState.Success(photo)
                } else {
                    _uiState.value = PhotoDetailState.Error("Photo not found")
                }
            } catch (e: Exception) {
                _uiState.value = PhotoDetailState.Error(e.message ?: "Failed to load photo")
            }
        }
    }

    fun updateMetadata(metadata: Map<String, Any>) {
        viewModelScope.launch {
            try {
                _uiState.value = PhotoDetailState.Saving
                photosRepository.updatePhotoMetadata(photoId, metadata)
                loadPhoto() // Reload the photo to show updated data
            } catch (e: Exception) {
                _uiState.value = PhotoDetailState.Error(e.message ?: "Failed to update photo")
            }
        }
    }

    fun deletePhoto() {
        viewModelScope.launch {
            try {
                _uiState.value = PhotoDetailState.Deleting
                photosRepository.deletePhoto(photoId)
                _uiState.value = PhotoDetailState.Deleted
            } catch (e: Exception) {
                _uiState.value = PhotoDetailState.Error(e.message ?: "Failed to delete photo")
            }
        }
    }
}

sealed class PhotoDetailState {
    object Loading : PhotoDetailState()
    object Saving : PhotoDetailState()
    object Deleting : PhotoDetailState()
    object Deleted : PhotoDetailState()
    data class Success(val photo: Photo) : PhotoDetailState()
    data class Error(val message: String) : PhotoDetailState()
}
