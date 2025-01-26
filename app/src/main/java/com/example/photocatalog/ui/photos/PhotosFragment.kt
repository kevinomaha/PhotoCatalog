package com.example.photocatalog.ui.photos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.photocatalog.R
import com.example.photocatalog.data.models.Photo
import com.example.photocatalog.databinding.FragmentPhotosBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PhotosFragment : Fragment() {
    private var _binding: FragmentPhotosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PhotosViewModel by viewModels()
    private lateinit var photosAdapter: PhotosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
        
        viewModel.loadPhotos()
    }

    private fun setupRecyclerView() {
        photosAdapter = PhotosAdapter(
            onPhotoClick = { photo -> navigateToPhotoDetail(photo) },
            onPhotoLongClick = { photo -> showPhotoOptions(photo) }
        )
        binding.photosRecyclerView.adapter = photosAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.photoState.collect { state ->
                when (state) {
                    is PhotoState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.emptyView.visibility = View.GONE
                    }
                    is PhotoState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        if (state.photos.isEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                        } else {
                            binding.emptyView.visibility = View.GONE
                            photosAdapter.submitList(state.photos)
                        }
                    }
                    is PhotoState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPhotos()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.fabTakePhoto.setOnClickListener {
            findNavController().navigate(R.id.action_photos_to_camera)
        }
    }

    private fun navigateToPhotoDetail(photo: Photo) {
        // TODO: Implement navigation to photo detail screen
        val action = PhotosFragmentDirections.actionPhotosToPhotoDetail(photo.id)
        findNavController().navigate(action)
    }

    private fun showPhotoOptions(photo: Photo) {
        // TODO: Show bottom sheet with photo options (delete, edit metadata, share)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
