package com.example.explorexpert

import com.example.explorexpert.data.repository.UserRepository
import com.example.explorexpert.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.junit.Assert.*
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AuthViewModelTest {
    @Mock
    private val mockAuth = mock<FirebaseAuth> {

    }

    @Mock
    private val mockUserRepo = mock<UserRepository> {

    }

    private val authViewModel = AuthViewModel(mockAuth, mockUserRepo)

    @Test
    fun invalid_email_has_email_error() {
        val invalidEmail = "explorexpertgmail.com"
        val validPassword = "1234567891011121314"
        val expectedLoginFormState = AuthViewModel.LoginFormState(
            emailError = "Email is not a valid email"
        )

        CoroutineScope(Dispatchers.Main).launch {
            authViewModel.loginDataChanged(invalidEmail, validPassword)

            authViewModel.loginFormState.observeForever { loginFormState ->
                assertEquals(expectedLoginFormState, loginFormState)
            }
        }
    }

    @Test
    fun invalid_password_has_password_error() {
        val validEmail = "explorexpert@gmail.com"
        val invalidPassword = "123"
        val expectedLoginFormState = AuthViewModel.LoginFormState(
            passwordError = "Password is too short"
        )

        CoroutineScope(Dispatchers.Main).launch {
            authViewModel.loginDataChanged(validEmail, invalidPassword)

            authViewModel.loginFormState.observeForever { loginFormState ->
                assertEquals(expectedLoginFormState, loginFormState)
            }
        }
    }

    @Test
    fun valid_email_and_password_has_data_valid() {
        val validEmail = "explorexpert2023@gmail.com"
        val validPassword = "abcdefghijklmnopqrstuvwxyz"
        val expectedLoginFormState = AuthViewModel.LoginFormState(
            isDataValid = true
        )

        CoroutineScope(Dispatchers.Main).launch {
            authViewModel.loginDataChanged(validEmail, validPassword)

            authViewModel.loginFormState.observeForever { loginFormState ->
                assertEquals(expectedLoginFormState, loginFormState)
            }
        }
    }
}