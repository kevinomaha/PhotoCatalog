package com.example.photocatalog.ui.photodetail

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.photocatalog.R
import com.example.photocatalog.data.models.Photo
import com.example.photocatalog.databinding.FragmentPhotoDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PhotoDetailFragment : Fragment() {
    private var _binding: FragmentPhotoDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PhotoDetailViewModel by viewModels()
    private var currentPhoto: Photo? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_photo_detail, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete -> {
                        showDeleteConfirmation()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is PhotoDetailState.Loading -> showLoading(true)
                    is PhotoDetailState.Success -> {
                        showLoading(false)
                        displayPhoto(state.photo)
                    }
                    is PhotoDetailState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                    is PhotoDetailState.Saving -> showLoading(true)
                    is PhotoDetailState.Deleting -> showLoading(true)
                    is PhotoDetailState.Deleted -> {
                        showLoading(false)
                        navigateBack()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            fabShare.setOnClickListener { sharePhoto() }
            locationButton.setOnClickListener { navigateToLocation() }
            
            // Save metadata when focus is lost
            observationsEdit.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) saveMetadata()
            }
            
            costEdit.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) saveMetadata()
            }
            
            ratingBar.setOnRatingBarChangeListener { _, _, _ ->
                saveMetadata()
            }
        }
    }

    private fun displayPhoto(photo: Photo) {
        currentPhoto = photo
        binding.apply {
            Glide.with(photoImage)
                .load(photo.url)
                .into(photoImage)

            observationsEdit.setText(photo.observations)
            costEdit.setText(photo.cost?.toString() ?: "")
            ratingBar.rating = photo.rating?.toFloat() ?: 0f

            // Format and display date
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            dateText.text = photo.timestamp?.let { 
                getString(R.string.taken_on, dateFormat.format(it))
            } ?: ""

            // Update location button text if location exists
            locationButton.text = if (photo.location != null) {
                getString(R.string.view_location)
            } else {
                getString(R.string.add_location)
            }

            // Display tags
            tagsChipGroup.removeAllViews()
            photo.tags.forEach { tag ->
                // Add chip for each tag
            }
        }
    }

    private fun saveMetadata() {
        val metadata = mutableMapOf<String, Any>()
        
        binding.apply {
            val observations = observationsEdit.text?.toString()
            if (!observations.isNullOrBlank()) {
                metadata["observations"] = observations
            }

            val cost = costEdit.text?.toString()?.toDoubleOrNull()
            if (cost != null) {
                metadata["cost"] = cost
            }

            val rating = ratingBar.rating.toInt()
            if (rating > 0) {
                metadata["rating"] = rating
            }
        }

        if (metadata.isNotEmpty()) {
            viewModel.updateMetadata(metadata)
        }
    }

    private fun sharePhoto() {
        currentPhoto?.let { photo ->
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, photo.url)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_photo)))
        }
    }

    private fun navigateToLocation() {
        // TODO: Implement location navigation
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_photo)
            .setMessage(R.string.delete_photo_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deletePhoto()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        // TODO: Implement loading indicator
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
