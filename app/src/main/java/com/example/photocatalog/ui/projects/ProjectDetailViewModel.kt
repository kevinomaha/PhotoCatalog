package com.example.photocatalog.ui.projects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photocatalog.data.models.Photo
import com.example.photocatalog.data.models.Project
import com.example.photocatalog.data.source.PhotosRepository
import com.example.photocatalog.data.source.ProjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val photosRepository: PhotosRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle["projectId"])
    private val _uiState = MutableStateFlow<ProjectDetailState>(ProjectDetailState.Loading)
    val uiState: StateFlow<ProjectDetailState> = _uiState

    init {
        loadProject()
    }

    fun loadProject() {
        viewModelScope.launch {
            try {
                _uiState.value = ProjectDetailState.Loading
                val project = projectsRepository.getProject(projectId)
                val photos = photosRepository.getPhotosForProject(projectId)
                _uiState.value = ProjectDetailState.Success(project, photos)
            } catch (e: Exception) {
                _uiState.value = ProjectDetailState.Error(e.message ?: "Error loading project")
            }
        }
    }

    fun deleteProject() {
        viewModelScope.launch {
            try {
                _uiState.value = ProjectDetailState.Loading
                projectsRepository.deleteProject(projectId)
                _uiState.value = ProjectDetailState.Deleted
            } catch (e: Exception) {
                _uiState.value = ProjectDetailState.Error(e.message ?: "Error deleting project")
            }
        }
    }

    fun shareProject() {
        viewModelScope.launch {
            try {
                projectsRepository.shareProject(projectId)
                _uiState.value = ProjectDetailState.Shared
            } catch (e: Exception) {
                _uiState.value = ProjectDetailState.Error(e.message ?: "Error sharing project")
            }
        }
    }
}

sealed class ProjectDetailState {
    object Loading : ProjectDetailState()
    data class Success(val project: Project, val photos: List<Photo>) : ProjectDetailState()
    object Deleted : ProjectDetailState()
    object Shared : ProjectDetailState()
    data class Error(val message: String) : ProjectDetailState()
}
