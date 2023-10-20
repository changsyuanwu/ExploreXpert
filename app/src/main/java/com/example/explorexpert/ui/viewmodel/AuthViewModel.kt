package com.example.explorexpert.ui.viewmodel

import android.content.Intent
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explorexpert.data.model.User
import com.example.explorexpert.data.repository.UserRepository
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.R
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import javax.inject.Inject


class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepo: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    data class LoginFormState(
        val emailError: String? = null,
        val passwordError: String? = null,
        val isDataValid: Boolean = false
    )

    data class LoginResult(
        val isSuccess: Boolean,
        val message: String,
    )

    data class RegisterFormState(
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
        val isDataValid: Boolean = false
    )

    data class RegisterResult(
        val isSuccess: Boolean,
        val message: String,
    )

    private val _loginFormState = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginFormState

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _registerFormState = MutableLiveData<RegisterFormState>()
    val registerFormState: LiveData<RegisterFormState> = _registerFormState

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    lateinit var oneTapClient: SignInClient
    lateinit var signInRequest: BeginSignInRequest
    lateinit var signUpRequest: BeginSignInRequest
    private var showOneTapUI = true

    fun registerWithEmailAndPassword(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        // Create user in collection for custom fields
                        createUserInCollection(user.uid, firstName, lastName, user.email)

                        // Update display name
                        val displayName = "$firstName $lastName"
                        user.updateProfile(buildProfileUpdateRequest(displayName))
                            .addOnCompleteListener { updateTask ->
                                sendEmailVerification(user)
                                if (updateTask.isSuccessful) {
                                    _registerResult.value =
                                        RegisterResult(true, "Verification email sent")
                                } else {
                                    _registerResult.value = RegisterResult(
                                        false,
                                        "Registration successful but failed to update display name."
                                    )
                                    Log.d(TAG, "Registration successful but failed to update display name for UID ${user.uid}.")
                                }
                            }
                    }
                } else {
                    _registerResult.value =
                        RegisterResult(false, "Registration failed. Please try again later.")
                    Log.d(TAG, "Registration failed. ${task.exception?.message}")
                }
            }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _registerResult.value = RegisterResult(true, "Verification email sent.")
                } else {
                    _registerResult.value =
                        RegisterResult(false, "Failed to send verification email.")
                }
            }
    }

    private fun buildProfileUpdateRequest(displayName: String): UserProfileChangeRequest {
        return UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
    }

    fun loginWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        _loginResult.value = LoginResult(true, "Successfully logged in.")
                    } else {
                        _loginResult.value = LoginResult(false, "Please verify your email first.")
                    }
                } else {
                    _loginResult.value =
                        LoginResult(
                            false,
                            "Authentication failed. Please check your email and password."
                        )
                    Log.d(TAG, "Login attempt failed. ${task.exception?.message}")
                }
            }
    }

    // Google SSO related code adapted from Google's tutorial at https://developers.google.com/identity/sign-in/android/sign-in
    fun processGoogleSSOCredential(data: Intent?) {
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            when {
                idToken != null -> {
                    // Got an ID token from Google. Use it to authenticate with Firebase.
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    Log.d(TAG, "Got ID token.")

                    var firstName = credential.givenName
                    var lastName = credential.familyName

                    if (firstName == null)
                        firstName = ""
                    if (lastName == null)
                        lastName = ""

                    loginWithSSOCredential(firebaseCredential, firstName, lastName)
                }

                else -> {
                    // Shouldn't happen.
                    Log.d(TAG, "No ID token!")
                }
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    Log.d(TAG, "One-tap dialog was closed.")
                    // Don't re-prompt the user.
                    showOneTapUI = false
                }

                CommonStatusCodes.NETWORK_ERROR -> {
                    Log.d(TAG, "One-tap encountered a network error.")
                    // Try again or just ignore.
                }

                else -> {
                    Log.d(
                        TAG, "Couldn't get credential from result." +
                                " (${e.localizedMessage})"
                    )
                }
            }
        }
    }

    private fun loginWithSSOCredential(
        firebaseCredential: AuthCredential,
        firstName: String,
        lastName: String
    ) {
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update with the signed-in user's information
                    Log.d(TAG, "signInWithSSOCredential:success")
                    _loginResult.value = LoginResult(true, "Successfully logged in.")

                    // Create user in collection for custom fields
                    val user = auth.currentUser
                    if (user != null) {
                        createUserInCollection(
                            user.uid,
                            firstName,
                            lastName,
                            user.email,
                            verified = true
                        )
                    }
                } else {
                    // Sign in failed.
                    _loginResult.value =
                        LoginResult(false, "Authentication failed. ${task.exception?.message}")
                    Log.w(TAG, "signInWithSSOCredential:failure", task.exception)
                }
            }
    }

    private fun createUserInCollection(
        userId: String,
        firstName: String,
        lastName: String,
        email: String?,
        verified: Boolean = false
    ) {
        if (userId != null && firstName != null && lastName != null && email != null) {
            val user = User(userId, firstName, lastName, email, verified = verified)
            viewModelScope.launch {
                try {
                    if (userRepo.getUserById(userId) == null) {
                        userRepo.setUser(user)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating user in collection: ${e.message}", e)
                }
            }
        }
    }

    fun loginDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _loginFormState.value = LoginFormState(emailError = "Email is not a valid email")
        } else if (!isPasswordValid(password)) {
            _loginFormState.value = LoginFormState(passwordError = "Password is too short")
        } else {
            _loginFormState.value = LoginFormState(isDataValid = true)
        }
    }

    fun registerDataChanged(email: String, password: String, confirmPassword: String) {
        if (!isEmailValid(email)) {
            _registerFormState.value = RegisterFormState(emailError = "Email is not a valid email")
        } else if (!isPasswordValid(password)) {
            _registerFormState.value = RegisterFormState(passwordError = "Password is too short")
        } else if (!doPasswordsMatch(password, confirmPassword)) {
            _registerFormState.value = RegisterFormState(confirmPasswordError = "Passwords do not match!")
        } else {
            _registerFormState.value = RegisterFormState(isDataValid = true)
        }
    }

    // A basic username validation check
    // Just checks for wh
    private fun isEmailValid(email: String): Boolean {
        return if (email.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else {
            email.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 10
    }

    private fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun logout() {
        auth.signOut()
    }
}