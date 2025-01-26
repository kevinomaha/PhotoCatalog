package com.example.photocatalog.ui.projects

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photocatalog.data.models.Project
import com.example.photocatalog.data.source.ProjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProjectViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val projectId: String = checkNotNull(savedStateHandle["projectId"])
    private val _uiState = MutableStateFlow<EditProjectState>(EditProjectState.Loading)
    val uiState: StateFlow<EditProjectState> = _uiState

    private var coverImageUri: Uri? = null

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            try {
                val project = projectsRepository.getProject(projectId)
                _uiState.value = EditProjectState.Success(project)
            } catch (e: Exception) {
                _uiState.value = EditProjectState.Error(e.message ?: "Error loading project")
            }
        }
    }

    fun setCoverImage(uri: Uri) {
        coverImageUri = uri
    }

    fun updateProject(name: String, description: String) {
        if (name.isBlank()) {
            _uiState.value = EditProjectState.Error("Project name is required")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = EditProjectState.Loading
                
                projectsRepository.updateProject(
                    projectId = projectId,
                    name = name,
                    description = description,
                    coverImageUri = coverImageUri
                )
                
                _uiState.value = EditProjectState.Updated
            } catch (e: Exception) {
                _uiState.value = EditProjectState.Error(e.message ?: "Error updating project")
            }
        }
    }

    fun resetState() {
        loadProject()
    }
}

sealed class EditProjectState {
    object Loading : EditProjectState()
    data class Success(val project: Project) : EditProjectState()
    object Updated : EditProjectState()
    data class Error(val message: String) : EditProjectState()
}
