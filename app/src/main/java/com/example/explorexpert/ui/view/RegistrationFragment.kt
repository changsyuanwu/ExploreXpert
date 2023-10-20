package com.example.explorexpert.ui.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.explorexpert.databinding.FragmentRegistrationBinding
import com.example.explorexpert.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    @Inject
    lateinit var authViewModel: AuthViewModel

    private lateinit var navController: NavController
    private var _binding: FragmentRegistrationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        configureButtons()
        configureObservers()
    }

    private fun configureObservers() {
        val emailEditText = binding.email.editText
        val passwordEditText = binding.password.editText
        val confirmPasswordEditText = binding.confirmPassword.editText
        val signUpButton = binding.btnSignUp

        authViewModel.registerFormState.observe(viewLifecycleOwner,
            Observer { registerFormState ->
                if (registerFormState == null) {
                    return@Observer
                }
                signUpButton.isEnabled = registerFormState.isDataValid
                registerFormState.emailError?.let {
                    emailEditText?.error = it
                }
                registerFormState.passwordError?.let {
                    passwordEditText?.error = it
                }
                registerFormState.confirmPasswordError?.let {
                    confirmPasswordEditText?.error = it
                }
            })

        authViewModel.registerResult.observe(viewLifecycleOwner,
            Observer { registerResult ->
                registerResult ?: return@Observer
                if (registerResult.isSuccess) {
                    goBackToLoginPage()
                } else {
                    showRegisterFailed(registerResult.message)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                authViewModel.registerDataChanged(
                    emailEditText?.text.toString(),
                    passwordEditText?.text.toString(),
                    confirmPasswordEditText?.text.toString()
                )
            }
        }
        emailEditText?.addTextChangedListener(afterTextChangedListener)
        passwordEditText?.addTextChangedListener(afterTextChangedListener)
        confirmPasswordEditText?.addTextChangedListener(afterTextChangedListener)
    }

    private fun configureButtons() {
        binding.btnSignUp.setOnClickListener {
            authViewModel.registerWithEmailAndPassword(
                binding.email.editText?.text.toString(),
                binding.confirmPassword.editText?.text.toString(),
                binding.firstName.editText?.text.toString(),
                binding.lastName.editText?.text.toString(),
            )
        }

        binding.btnBack.setOnClickListener {
            goBackToLoginPage()
        }
    }

    private fun showRegisterFailed(errorString: String) {
        Toast.makeText(requireContext(), errorString, Toast.LENGTH_LONG).show()
    }

    private fun goBackToLoginPage() {
        findNavController().popBackStack()
    }
}