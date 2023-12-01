package com.example.explorexpert.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.explorexpert.R
import com.example.explorexpert.databinding.DialogEditProfileBinding
import com.example.explorexpert.ui.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditProfileDialogFragment(
): DialogFragment() {

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    private var _binding: DialogEditProfileBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FullScreenDialogSlideLeftStyle)
        profileViewModel.refreshCurrentUser()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgressIndicator()

        configureObservers()
        configureButtons()
    }

    private fun configureObservers() {
        profileViewModel.currentUser.observe(viewLifecycleOwner) { user ->

            binding.txtInputFirstName.editText?.setText(user.firstName)
            binding.txtInputLastName.editText?.setText(user.lastName)

            hideProgressIndicator()
        }
    }

    private fun configureButtons() {
        binding.btnBackIcon.setOnClickListener {
            this.dismiss()
        }

        binding.btnSave.setOnClickListener {
            val newFirstName = binding.txtInputFirstName.editText?.text.toString()
            val newLastName = binding.txtInputLastName.editText?.text.toString()

            if (newFirstName == "") {
                binding.txtInputFirstName.error = "First name cannot be empty"
                return@setOnClickListener
            }
            else {
                binding.txtInputFirstName.error = null
            }
            if (newLastName == "") {
                binding.txtInputLastName.error = "Last name cannot be empty"
                return@setOnClickListener
            }
            else {
                binding.txtInputLastName.error = null
            }

            profileViewModel.updateUser(newFirstName, newLastName)

            refreshParentFragment()
            this.dismiss()
        }
    }

    private fun refreshParentFragment() {
        (requireParentFragment() as HomeFragment).refreshCurrentUser()
        (requireParentFragment() as HomeFragment).scheduleCurrentUserRefresh()
    }

    private fun showProgressIndicator() {
        binding.progressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        binding.progressIndicator.visibility = View.INVISIBLE
    }

    companion object {
        const val TAG: String = "EditProfileDialogFragment"
    }
}