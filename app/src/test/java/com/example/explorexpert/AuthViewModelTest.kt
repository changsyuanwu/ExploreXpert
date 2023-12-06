package com.example.explorexpert

import com.example.explorexpert.data.repository.UserRepository
import com.example.explorexpert.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import org.junit.Assert
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock
import org.junit.Assert.*

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

        authViewModel.loginDataChanged(invalidEmail, validPassword)

        val expectedLoginFormState = AuthViewModel.LoginFormState(
            emailError = "Email is not a valid email"
        )

        assertEquals(expectedLoginFormState, authViewModel.loginFormState.value)
    }

    @Test
    fun invalid_password_has_password_error() {
        val validEmail = "explorexpert@gmail.com"
        val invalidPassword = "123"

        authViewModel.loginDataChanged(validEmail, invalidPassword)

        val expectedLoginFormState = AuthViewModel.LoginFormState(
            passwordError = "Password is too short"
        )

        assertEquals(expectedLoginFormState, authViewModel.loginFormState.value)
    }


}