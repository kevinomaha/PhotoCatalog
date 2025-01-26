package com.example.photocatalog.ui.projects

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photocatalog.data.source.ProjectsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateProjectState>(CreateProjectState.Initial)
    val uiState: StateFlow<CreateProjectState> = _uiState

    private var coverImageUri: Uri? = null

    fun setCoverImage(uri: Uri) {
        coverImageUri = uri
    }

    fun createProject(name: String, description: String) {
        if (name.isBlank()) {
            _uiState.value = CreateProjectState.Error("Project name is required")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = CreateProjectState.Loading
                
                projectsRepository.createProject(
                    name = name,
                    description = description,
                    coverImageUri = coverImageUri
                )
                
                _uiState.value = CreateProjectState.Success
            } catch (e: Exception) {
                _uiState.value = CreateProjectState.Error(e.message ?: "Error creating project")
            }
        }
    }

    fun resetState() {
        _uiState.value = CreateProjectState.Initial
    }
}

sealed class CreateProjectState {
    object Initial : CreateProjectState()
    object Loading : CreateProjectState()
    object Success : CreateProjectState()
    data class Error(val message: String) : CreateProjectState()
}
