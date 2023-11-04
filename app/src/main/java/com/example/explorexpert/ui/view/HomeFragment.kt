package com.example.explorexpert.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.explorexpert.R
import com.example.explorexpert.SplashScreenActivity
import com.example.explorexpert.databinding.FragmentHomeBinding
import com.example.explorexpert.ui.viewmodel.HomeViewModel
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBarLayout.statusBarForeground =
            MaterialShapeDrawable.createWithElevationOverlay(context)

        configureButtons()
        configureNavSideBar()
        configureUserDetails()
    }

    private fun configureUserDetails() {
        CoroutineScope(Dispatchers.Main).launch {
            val currentUserName = homeViewModel.getCurrentUserName()
            if (currentUserName != "") {
                binding.txtName.text = currentUserName
                binding.navigationViewSideBar.findViewById<TextView>(R.id.txtNameSideNav).text =
                    currentUserName
            }

            val currentUserEmail = homeViewModel.getCurrentUserEmail()
            if (currentUserEmail != "") {
                binding.navigationViewSideBar.findViewById<TextView>(R.id.txtEmailSideNav).text =
                    currentUserEmail
            }
        }
    }

    private fun configureNavSideBar() {
        binding.navigationViewSideBar.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btnLogout -> logOut()
                // Add other buttons for side nav bar here
                else -> {
                    return@setNavigationItemSelectedListener false
                }
            }
            true
        }
    }

    private fun logOut() {
        homeViewModel.logOut()
        val intent = Intent(requireContext(), SplashScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun configureButtons() {
        binding.btnMenuIcon.setOnClickListener {
            binding.drawerLayout.open()
        }
    }
}