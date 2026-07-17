package com.example.foodapp.presentation.auth

data class LoginState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val showOtpInput: Boolean = false,
    val errorMessage: String? = null
) {
    val isFormValid: Boolean
        get() = when {
            showOtpInput -> otpCode.length == 6
            else -> phoneNumber.isNotEmpty()
        }
}