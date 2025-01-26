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
import com.example.photocatalog.databinding.FragmentCreateProjectBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateProjectFragment : Fragment() {
    private var _binding: FragmentCreateProjectBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateProjectViewModel by viewModels()

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
        _binding = FragmentCreateProjectBinding.inflate(inflater, container, false)
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
                    is CreateProjectState.Initial -> {
                        binding.progressBar.visibility = View.GONE
                    }
                    is CreateProjectState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is CreateProjectState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, R.string.project_created, Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                    is CreateProjectState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun saveProject() {
        val name = binding.nameEdit.text.toString().trim()
        val description = binding.descriptionEdit.text.toString().trim()
        viewModel.createProject(name, description)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
