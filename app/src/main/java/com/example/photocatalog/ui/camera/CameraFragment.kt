package com.example.photocatalog.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.photocatalog.R
import com.example.photocatalog.data.models.PhotoUpload
import com.example.photocatalog.databinding.FragmentCameraBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CameraViewModel by viewModels()
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(context, R.string.error_camera_permission, Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
        checkCameraPermission()
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cameraState.collect { state ->
                when (state) {
                    is CameraState.Initial -> {
                        // Initial state, do nothing
                    }
                    is CameraState.Starting -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is CameraState.Preview -> {
                        binding.progressBar.visibility = View.GONE
                        binding.captureButton.isEnabled = true
                    }
                    is CameraState.Capturing -> {
                        binding.captureButton.isEnabled = false
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is CameraState.PhotoCaptured -> {
                        binding.progressBar.visibility = View.GONE
                        binding.captureButton.isEnabled = true
                        uploadPhoto(state.file, state.uri)
                    }
                    is CameraState.Uploading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is CameraState.Uploaded -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, "Photo uploaded successfully", Toast.LENGTH_SHORT).show()
                    }
                    is CameraState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.captureButton.isEnabled = true
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                    is CameraState.SwitchingCamera -> {
                        startCamera()
                    }
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.captureButton.setOnClickListener {
            viewModel.capturePhoto()
        }
        
        binding.switchCameraButton.setOnClickListener {
            viewModel.switchCamera()
        }
    }
    
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(context, R.string.error_camera_permission, Toast.LENGTH_LONG).show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun startCamera() {
        viewModel.startCamera(viewLifecycleOwner, binding.viewFinder)
    }
    
    private fun uploadPhoto(file: java.io.File, uri: android.net.Uri) {
        val photoUpload = PhotoUpload(
            file = file,
            uri = uri
        )
        viewModel.uploadPhoto(photoUpload)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
