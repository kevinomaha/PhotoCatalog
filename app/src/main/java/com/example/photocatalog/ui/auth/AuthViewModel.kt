package com.example.photocatalog.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photocatalog.data.source.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        _authState.value = if (authRepository.isUserSignedIn()) {
            AuthState.SignedIn(authRepository.getCurrentUser()?.displayName ?: "")
        } else {
            AuthState.SignedOut
        }
    }

    fun handleSignInResult(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            try {
                if (account == null) {
                    _authState.value = AuthState.Error("Sign in failed")
                    return@launch
                }

                authRepository.firebaseAuthWithGoogle(account)
                _authState.value = AuthState.SignedIn(account.displayName ?: "")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _authState.value = AuthState.SignedOut
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign out failed")
            }
        }
    }
}

sealed class AuthState {
    object SignedOut : AuthState()
    data class SignedIn(val userName: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
