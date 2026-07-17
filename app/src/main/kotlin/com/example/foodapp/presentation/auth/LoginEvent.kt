package com.example.foodapp.presentation.auth

sealed class LoginEvent {
    // Social login events
    object GoogleLoginClick : LoginEvent()
    object FacebookLoginClick : LoginEvent()

    // Phone number events
    data class PhoneNumberChanged(val phoneNumber: String) : LoginEvent()
    object RequestOtpClick : LoginEvent()

    // OTP events
    data class OtpCodeChanged(val otpCode: String) : LoginEvent()
    object ConfirmOtpClick : LoginEvent()
}

// Authentication events for Supabase/gotrue
sealed class AuthEvent {
    object GoogleLogin : AuthEvent()
    object FacebookLogin : AuthEvent()
    object OtpLogin : AuthEvent()
}