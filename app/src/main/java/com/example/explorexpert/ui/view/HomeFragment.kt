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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.explorexpert.R
import com.example.explorexpert.SplashScreenActivity
import com.example.explorexpert.adapters.NearbyPlaceAdapter
import com.example.explorexpert.data.model.NearbyPlace
import com.example.explorexpert.data.model.User
import com.example.explorexpert.data.repository.TripRepository
import com.example.explorexpert.databinding.FragmentHomeBinding
import com.example.explorexpert.ui.viewmodel.HomeViewModel
import com.example.explorexpert.utils.ImageLoaderUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.libraries.places.api.Places
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    @Inject
    lateinit var homeViewModel: HomeViewModel

    @Inject
    lateinit var tripRepo: TripRepository

    private lateinit var appInfo: ApplicationInfo

    private lateinit var nearbyPlacesAdapter: NearbyPlaceAdapter

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
        configureNearbyPlacesToExplore()
        configureNearbyPlacesRecyclerView()
    }

    private fun configurePlacesSDK() {
        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized()) {
            if (appId != null) {
                Places.initialize(requireContext(), appId)
            }
        }
        val placesClient = Places.createClient(requireContext())
    }

    private fun configureNearbyPlacesToExplore() {
        CoroutineScope(Dispatchers.Main).launch {
            getNearbyLocations()
        }
    }

    private suspend fun getNearbyLocations() {
        val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

        val currentLocation = getCurrentLocation()

        if (currentLocation == null) {
            return
        }

        val nearbySearchReq = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${currentLocation.latitude}%2C${currentLocation.longitude}" +
                "&radius=10000" + // Radius in meters
                "&type=tourist_attraction" +
                "&key=$apiKey"

        val queue = Volley.newRequestQueue(requireContext())

        var collected = 0

        val stringReq = StringRequest(
            Request.Method.GET,
            nearbySearchReq,
            { response ->
                try {
                    val obj = JSONObject(response)
                    val results = obj.getJSONArray("results")

                    val nearbyPlaces = mutableListOf<NearbyPlace>()

                    for (i in 0 until results.length()) {
                        val currentResult = results.getJSONObject(i)
                        val id = currentResult.getString("place_id")
                        val name = currentResult.getString("name")
                        val rating = currentResult.getDouble("rating")
                        val numRatings = currentResult.getInt("user_ratings_total")
                        val type = currentResult.getJSONArray("types").getString(0)

                        val currentPlace = NearbyPlace(
                            id,
                            name,
                            rating,
                            numRatings,
                            type,
                        )
                        nearbyPlaces.add(currentPlace)
                    }

                    homeViewModel.setNearbyPlaces(nearbyPlaces)

                } catch (e: Exception) {
                    Log.e(TAG, "Error while parsing nearby places: ${e.message}", e)
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e(TAG, "Error getting nearby places: ${error.message}")
            }
        )

        queue.add(stringReq)
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

        return fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
    }

    private fun configureUserDetails(user: User) {
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

        if (user.profilePictureURL != null) {
            val sideBarUserProfilePictureImgView = binding.navigationViewSideBar.findViewById<CircleImageView>(R.id.imgSideBarProfilePic)

            ImageLoaderUtil.loadImageIntoView(binding.imgProfilePic, user.profilePictureURL!!)

            if (sideBarUserProfilePictureImgView != null) {
                ImageLoaderUtil.loadImageIntoView(sideBarUserProfilePictureImgView, user.profilePictureURL!!)
            }
        }
    }

    private fun configureNearbyPlacesRecyclerView() {
        nearbyPlacesAdapter = NearbyPlaceAdapter(
            itemClickListener = object : NearbyPlaceAdapter.ItemClickListener {
                override fun onItemClick(nearbyPlace: NearbyPlace) {
                   // Do something
                }
            },
            tripRepo = tripRepo
        )

        binding.nearbyPlacesRecyclerView.adapter = nearbyPlacesAdapter

        val nearbyPlacesLayoutManager = LinearLayoutManager(requireContext())
        nearbyPlacesLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.nearbyPlacesRecyclerView.layoutManager = nearbyPlacesLayoutManager
    }

    private fun configureObservers() {
        homeViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            configureUserDetails(user)
        }

        homeViewModel.nearbyPlaces.observe(viewLifecycleOwner) { nearbyPlaces ->
            nearbyPlacesAdapter.submitList(nearbyPlaces)
            hideProgressIndicator()
            Log.d(TAG, nearbyPlaces.toString())
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