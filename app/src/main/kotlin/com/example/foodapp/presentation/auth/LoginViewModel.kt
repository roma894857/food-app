package com.example.foodapp.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.foodapp.data.SupabaseModule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.exception.AuthException
import io.github.jan.supabase.auth.exception.InvalidCredentialsException
import io.github.jan.supabase.postgrest.postgrest

class LoginViewModel(private val navController: NavController? = null) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            // Google login event
            LoginEvent.GoogleLoginClick -> {
                handleGoogleLogin()
            }

            // Facebook login event
            LoginEvent.FacebookLoginClick -> {
                handleFacebookLogin()
            }

            // Phone number change
            is LoginEvent.PhoneNumberChanged -> {
                _state.update { it.copy(phoneNumber = event.phoneNumber) }
            }

            // Request OTP event
            LoginEvent.RequestOtpClick -> {
                requestOtp()
            }

            // OTP code change
            is LoginEvent.OtpCodeChanged -> {
                _state.update { it.copy(otpCode = event.otpCode) }
            }

            // Confirm OTP event
            LoginEvent.ConfirmOtpClick -> {
                confirmOtp()
            }

            // Navigation event for successful login
            is LoginEvent.OauthSuccess -> {
                _state.update { it.copy(isLoading = false, errorMessage = null) }
                navController?.navigate("home")
            }
        }
    }

    private fun handleGoogleLogin() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // Use Supabase's Google auth provider
                SupabaseModule.getAuth().signInWith(GoogleProvider) {
                    // This should be set up in your Supabase project
                    redirectUrl = "foodapp://oauth/redirect"
                }

                // On successful login, navigate to home
                _state.update { it.copy(isLoading = false) }
                navController?.navigate("home")
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = when (e) {
                            is AuthException -> when (e) {
                                is InvalidCredentialsException -> "Invalid credentials"
                                else -> "Authentication failed: ${e.message}"
                            }
                            else -> "Google login failed: ${e.message}"
                        }
                    )
                }
                Log.e("LoginViewModel", "Google login error", e)
            }
        }
    }

    private fun handleFacebookLogin() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // Use Supabase's Facebook auth provider
                SupabaseModule.getAuth().signInWith(FacebookProvider) {
                    redirectUrl = "foodapp://oauth/redirect"
                }

                // On successful login, navigate to home
                _state.update { it.copy(isLoading = false) }
                navController?.navigate("home")
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = when (e) {
                            is AuthException -> when (e) {
                                is InvalidCredentialsException -> "Invalid credentials"
                                else -> "Authentication failed: ${e.message}"
                            }
                            else -> "Facebook login failed: ${e.message}"
                        }
                    )
                }
                Log.e("LoginViewModel", "Facebook login error", e)
            }
        }
    }

    private fun requestOtp() {
        if (_state.value.phoneNumber.isEmpty()) {
            _state.update {
                it.copy(errorMessage = "Please enter phone number")
            }
            return
        }

        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Simulate OTP sending via Telegram (as per requirements)
                delay(2000)

                _state.update {
                    it.copy(
                        isLoading = false,
                        showOtpInput = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to send OTP: ${e.message}"
                    )
                }
                Log.e("LoginViewModel", "OTP request error", e)
            }
        }
    }

    private fun confirmOtp() {
        if (_state.value.otpCode.length != 6) {
            _state.update {
                it.copy(errorMessage = "Please enter valid 6-digit OTP")
            }
            return
        }

        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Simulate OTP verification via Telegram (as per requirements)
                delay(2000)

                // For real implementation, this would verify OTP via Telegram bot
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }

                // Navigate to home after successful OTP verification
                navController?.navigate("home")
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to verify OTP: ${e.message}"
                    )
                }
                Log.e("LoginViewModel", "OTP verification error", e)
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
