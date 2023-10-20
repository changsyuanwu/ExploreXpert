package com.example.explorexpert.ui.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope

import com.example.explorexpert.R
import com.example.explorexpert.data.model.User
import com.example.explorexpert.data.repository.UserRepository
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import javax.inject.Inject


class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepo: UserRepository
) : ViewModel() {

    companion object {
        private val TAG = "AuthViewModel"
    }

    data class LoginFormState(
        val usernameError: Int? = null,
        val passwordError: Int? = null,
        val isDataValid: Boolean = false
    )

    data class LoginResult(
        val isSuccess: Boolean,
        val message: String,
    )


    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    lateinit var oneTapClient: SignInClient
    lateinit var signInRequest: BeginSignInRequest
    lateinit var signUpRequest: BeginSignInRequest
    private var showOneTapUI = true

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
                            user.email
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

    private fun createUserInCollection(userId: String, firstName: String, lastName: String, email: String?) {
        if (userId != null && firstName != null && lastName != null && email != null) {
            val user = User(userId, firstName, lastName, email)
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

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}