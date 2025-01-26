package com.example.photocatalog.ui.projects

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
class ProjectsViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsState>(ProjectsState.Loading)
    val uiState: StateFlow<ProjectsState> = _uiState

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            try {
                _uiState.value = ProjectsState.Loading
                val projects = projectsRepository.getProjects()
                _uiState.value = ProjectsState.Success(projects)
            } catch (e: Exception) {
                _uiState.value = ProjectsState.Error(e.message ?: "Failed to load projects")
            }
        }
    }

    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            try {
                projectsRepository.deleteProject(projectId)
                loadProjects() // Reload the list
            } catch (e: Exception) {
                _uiState.value = ProjectsState.Error(e.message ?: "Failed to delete project")
            }
        }
    }
}

sealed class ProjectsState {
    object Loading : ProjectsState()
    data class Success(val projects: List<Project>) : ProjectsState()
    data class Error(val message: String) : ProjectsState()
}
