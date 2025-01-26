package com.example.photocatalog.ui.projects

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.photocatalog.R
import com.example.photocatalog.data.models.Photo
import com.example.photocatalog.data.models.Project
import com.example.photocatalog.databinding.FragmentProjectDetailBinding
import com.example.photocatalog.ui.photos.PhotosAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProjectDetailFragment : Fragment() {
    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProjectDetailViewModel by viewModels()
    private lateinit var photosAdapter: PhotosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_share -> {
                    viewModel.shareProject()
                    true
                }
                R.id.action_edit -> {
                    navigateToEditProject()
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        photosAdapter = PhotosAdapter(
            onPhotoClick = { photo -> navigateToPhotoDetail(photo) }
        )
        binding.photosRecyclerView.adapter = photosAdapter
    }

    private fun setupListeners() {
        binding.fabAddPhoto.setOnClickListener {
            navigateToCamera()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ProjectDetailState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is ProjectDetailState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        populateProject(state.project)
                        updatePhotosList(state.photos)
                    }
                    is ProjectDetailState.Deleted -> {
                        binding.progressBar.visibility = View.GONE
                        findNavController().navigateUp()
                    }
                    is ProjectDetailState.Shared -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, R.string.project_shared, Toast.LENGTH_SHORT).show()
                    }
                    is ProjectDetailState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun populateProject(project: Project) {
        binding.apply {
            collapsingToolbar.title = project.name
            descriptionText.text = project.description
            photoCount.text = resources.getQuantityString(
                R.plurals.photo_count,
                project.photoCount,
                project.photoCount
            )

            project.thumbnailUrl?.let { url ->
                Glide.with(this@ProjectDetailFragment)
                    .load(url)
                    .centerCrop()
                    .into(coverImage)
            }
        }
    }

    private fun updatePhotosList(photos: List<Photo>) {
        binding.emptyView.visibility = if (photos.isEmpty()) View.VISIBLE else View.GONE
        photosAdapter.submitList(photos)
    }

    private fun navigateToEditProject() {
        val action = ProjectDetailFragmentDirections.actionProjectDetailToEditProject(
            projectId = checkNotNull(arguments?.getString("projectId"))
        )
        findNavController().navigate(action)
    }

    private fun navigateToPhotoDetail(photo: Photo) {
        val action = ProjectDetailFragmentDirections.actionProjectDetailToPhotoDetail(photo.id)
        findNavController().navigate(action)
    }

    private fun navigateToCamera() {
        val action = ProjectDetailFragmentDirections.actionProjectDetailToCamera(
            projectId = checkNotNull(arguments?.getString("projectId"))
        )
        findNavController().navigate(action)
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_project)
            .setMessage(R.string.delete_project_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteProject()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
