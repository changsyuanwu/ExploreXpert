package com.example.explorexpert.ui.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.explorexpert.R
import com.example.explorexpert.SplashScreenActivity
import com.example.explorexpert.databinding.FragmentHomeBinding
import com.example.explorexpert.ui.viewmodel.HomeViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.material.shape.MaterialShapeDrawable
import com.urmich.android.placesearchktx.placesearch.search.NearbySearch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var homeViewModel: HomeViewModel

    private lateinit var appInfo: ApplicationInfo

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val TAG: String = "HomeFragment"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appInfo = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.refreshCurrentUser()
    }

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

        showProgressIndicator()

        configurePlacesSDK()
        configureButtons()
        configureObservers()
        configureNavSideBar()
        configurePlacesToExplore()

    }

    private fun configurePlacesSDK() {
        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), appId)
        }
        val placesClient = Places.createClient(requireContext())
    }

    private fun configurePlacesToExplore() {
        CoroutineScope(Dispatchers.Main).launch {
            val currentLocation = getCurrentLocation()

            if (currentLocation != null) {
                val currentLatLng = com.google.android.gms.maps.model.LatLng(
                    currentLocation.latitude,
                    currentLocation.longitude
                )

                val nearbySearch = NearbySearch.Builder()
                    .setType(Place.Type.POINT_OF_INTEREST)
                    .setRadius(10000) // radius in meters
                    .setLocation(currentLatLng)
                    .build()

                lifecycleScope.launch {
                    val response = nearbySearch.call()

                    if (response?.status == "OK") {
                        response.places.forEach {

                        }
                    } else {
                        Log.i(TAG, response.toString())
                    }
                }
            }
        }
    }

    private suspend fun getCurrentLocation(): Location? {
        // Check location permissions
        if (
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requireActivity().requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            requireActivity().requestPermissions(
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return null
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val currentLocation =
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()

        return currentLocation
    }

    private fun configureUserDetails() {
        val currentUserName = homeViewModel.getCurrentUserName()
        if (currentUserName != "") {
            binding.txtName.text = currentUserName

            val sideBarNameTextView = binding.navigationViewSideBar.findViewById<TextView>(R.id.txtNameSideNav)
            if (sideBarNameTextView != null) {
                sideBarNameTextView.text = currentUserName
            }
        }

        val currentUserEmail = homeViewModel.getCurrentUserEmail()
        if (currentUserEmail != "") {
            val sideBarEmailTextView = binding.navigationViewSideBar.findViewById<TextView>(R.id.txtEmailSideNav)
            if (sideBarEmailTextView != null) {
                sideBarEmailTextView.text = currentUserEmail
            }
        }
    }

    private fun configureObservers() {
        homeViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            configureUserDetails()
            hideProgressIndicator()
        }
    }

    private fun configureNavSideBar() {
        binding.navigationViewSideBar.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btnLogout -> logOut()

                R.id.btnEditProfile -> {
                    val editProfileDialogFragment = EditProfileDialogFragment()
                    editProfileDialogFragment.show(
                        childFragmentManager,
                        "editProfileDialog"
                    )
                }
                // Add other buttons for side nav bar here
                else -> {
                    return@setNavigationItemSelectedListener false
                }
            }
            true
        }
    }

    fun refreshCurrentUser() {
        homeViewModel.refreshCurrentUser()
    }

    fun scheduleCurrentUserRefresh() {
        val timer = Timer()
        var executionCount = 0

        timer.scheduleAtFixedRate(
            timerTask {
                if (executionCount > 5) {
                    this.cancel()
                }
                executionCount++
                refreshCurrentUser()
            },
            300,
            1000
        )
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

        binding.cardUserProfile.setOnClickListener {
            binding.drawerLayout.open()
        }
    }

    private fun showProgressIndicator() {
        binding.progressIndicator.visibility = View.VISIBLE
    }

    private fun hideProgressIndicator() {
        binding.progressIndicator.visibility = View.INVISIBLE
    }
}