package com.example.photocatalog.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.photocatalog.R
import com.example.photocatalog.databinding.FragmentSettingsBinding
import com.example.photocatalog.ui.auth.AuthState
import com.example.photocatalog.ui.auth.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by viewModels()
    
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            viewModel.handleSignInResult(account)
        } catch (e: ApiException) {
            viewModel.handleSignInResult(null)
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupObservers() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.SignedIn -> {
                    binding.buttonSignIn.visibility = View.GONE
                    binding.buttonSignOut.visibility = View.VISIBLE
                    binding.textUserName.text = state.userName
                    binding.textUserName.visibility = View.VISIBLE
                }
                is AuthState.SignedOut -> {
                    binding.buttonSignIn.visibility = View.VISIBLE
                    binding.buttonSignOut.visibility = View.GONE
                    binding.textUserName.visibility = View.GONE
                }
                is AuthState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun setupClickListeners() {
        binding.buttonSignIn.setOnClickListener {
            signInLauncher.launch(viewModel.getSignInIntent())
        }
        
        binding.buttonSignOut.setOnClickListener {
            viewModel.signOut()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
