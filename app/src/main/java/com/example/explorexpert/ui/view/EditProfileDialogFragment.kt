package com.example.explorexpert.ui.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.explorexpert.R
import com.example.explorexpert.data.model.User
import com.example.explorexpert.databinding.DialogEditProfileBinding
import com.example.explorexpert.ui.viewmodel.ProfileViewModel
import com.example.explorexpert.utils.ImageLoaderUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class EditProfileDialogFragment(
): DialogFragment() {

    @Inject
    lateinit var profileViewModel: ProfileViewModel

    private var _binding: DialogEditProfileBinding? = null

    private val binding get() = _binding!!

     // Registers a photo picker activity launcher in single-select mode.
    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            // Show loading icon and update image in UI
            showProfilePictureProgressIndicator()
            binding.imgProfilePic.setImageURI(uri)

            wasProfilePhotoChanged = true
            newProfilePhotoUri = uri

            hideProfilePictureProgressIndicator()
        } else {
            Log.d(TAG, "No media selected from profile photo picker")
        }
    }

    private var wasProfilePhotoChanged = false
    private var newProfilePhotoUri: Uri? = null
    private var oldProfilePhotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FullScreenDialogSlideLeftStyle)
        profileViewModel.refreshCurrentUser()
        oldProfilePhotoUrl = profileViewModel.currentUser.value?.profilePictureURL.toString()
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

    private fun configureUI(user: User) {
        binding.txtInputFirstName.editText?.setText(user.firstName)
        binding.txtInputLastName.editText?.setText(user.lastName)

        if (user.profilePictureURL != null) {
            ImageLoaderUtil.loadImageIntoView(binding.imgProfilePic, user.profilePictureURL!!)
            hideProfilePictureProgressIndicator()
        }
        else {
            hideProfilePictureProgressIndicator()
        }
    }

    private fun configureObservers() {
        profileViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            configureUI(user)

            hideProgressIndicator()
        }
    }

    private fun configureButtons() {
        binding.btnBackIcon.setOnClickListener {
            refreshParentFragment()
            this.dismiss()
        }

        binding.btnEditPhoto.setOnClickListener {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.btnSave.setOnClickListener {
            val newFirstName = binding.txtInputFirstName.editText?.text.toString()
            val newLastName = binding.txtInputLastName.editText?.text.toString()

            // Prevent use from saving with empty name fields
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

            if (wasProfilePhotoChanged) {
                profileViewModel.updateUserProfilePicture(newProfilePhotoUri!!)
            }

            refreshUserNowAndLater()
            refreshParentFragment()
            this.dismiss()
        }
    }

    private fun refreshUserNowAndLater() {
        refreshUser()
        scheduleUserRefresh()
    }

    private fun refreshUser() {
        profileViewModel.refreshCurrentUser()
    }

    private fun scheduleUserRefresh() {
        val timer = Timer()
        var executionCount = 0

        timer.scheduleAtFixedRate(
            timerTask {
                if (executionCount > 5) {
                    this.cancel()
                }
                executionCount++
                refreshUser()
            },
            300,
            1000
        )
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

    private fun showProfilePictureProgressIndicator() {
        binding.profilePictureProgressIndicator.visibility = View.VISIBLE
    }

    private fun hideProfilePictureProgressIndicator() {
        binding.profilePictureProgressIndicator.visibility = View.INVISIBLE
    }

    companion object {
        const val TAG: String = "EditProfileDialogFragment"
    }
}