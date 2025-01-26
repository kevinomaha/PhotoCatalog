package com.example.photocatalog.ui.projects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.photocatalog.R
import com.example.photocatalog.data.models.Project
import com.example.photocatalog.databinding.FragmentProjectsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProjectsFragment : Fragment() {
    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProjectsViewModel by viewModels()
    private lateinit var projectsAdapter: ProjectsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        projectsAdapter = ProjectsAdapter(
            onProjectClick = { project -> navigateToProject(project) },
            onProjectOptionsClick = { project, view -> showProjectOptions(project, view) }
        )
        binding.projectsRecyclerView.adapter = projectsAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ProjectsState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.emptyView.visibility = View.GONE
                    }
                    is ProjectsState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if (state.projects.isEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                        } else {
                            binding.emptyView.visibility = View.GONE
                            projectsAdapter.submitList(state.projects)
                        }
                    }
                    is ProjectsState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadProjects()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.fabAddProject.setOnClickListener {
            navigateToCreateProject()
        }
    }

    private fun navigateToProject(project: Project) {
        val action = ProjectsFragmentDirections.actionProjectsToProjectDetail(project.id)
        findNavController().navigate(action)
    }

    private fun navigateToCreateProject() {
        val action = ProjectsFragmentDirections.actionProjectsToCreateProject()
        findNavController().navigate(action)
    }

    private fun showProjectOptions(project: Project, anchorView: View) {
        PopupMenu(requireContext(), anchorView).apply {
            menuInflater.inflate(R.menu.menu_project_options, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        navigateToEditProject(project)
                        true
                    }
                    R.id.action_delete -> {
                        showDeleteConfirmation(project)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun navigateToEditProject(project: Project) {
        val action = ProjectsFragmentDirections.actionProjectsToEditProject(project.id)
        findNavController().navigate(action)
    }

    private fun showDeleteConfirmation(project: Project) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_project)
            .setMessage(getString(R.string.delete_project_confirmation, project.name))
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteProject(project.id)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
