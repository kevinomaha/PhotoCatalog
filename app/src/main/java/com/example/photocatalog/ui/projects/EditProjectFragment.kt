package com.example.photocatalog.ui.projects

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.photocatalog.R
import com.example.photocatalog.data.models.Project
import com.example.photocatalog.databinding.FragmentEditProjectBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProjectFragment : Fragment() {
    private var _binding: FragmentEditProjectBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProjectViewModel by viewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewModel.setCoverImage(uri)
                binding.coverImagePrompt.visibility = View.GONE
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(binding.coverImage)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListeners()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save -> {
                    saveProject()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupListeners() {
        binding.coverImageCard.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is EditProjectState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is EditProjectState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        populateProject(state.project)
                    }
                    is EditProjectState.Updated -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, R.string.project_updated, Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is EditProjectState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun populateProject(project: Project) {
        binding.apply {
            nameEdit.setText(project.name)
            descriptionEdit.setText(project.description)
            
            project.thumbnailUrl?.let { url ->
                coverImagePrompt.visibility = View.GONE
                Glide.with(this@EditProjectFragment)
                    .load(url)
                    .centerCrop()
                    .into(coverImage)
            }
        }
    }

    private fun saveProject() {
        val name = binding.nameEdit.text.toString().trim()
        val description = binding.descriptionEdit.text.toString().trim()
        viewModel.updateProject(name, description)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
