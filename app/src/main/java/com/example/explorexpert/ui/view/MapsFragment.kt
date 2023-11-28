package com.example.explorexpert.ui.view

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.explorexpert.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.io.IOException


class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback,
                        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    private var currLatLng: LatLng? = null
    private var currAddress: String = "";

    private var defaultLatLng = LatLng(43.4723, -80.5449)

    private lateinit var appInfo: ApplicationInfo
    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var selectLocationButton: Button

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        appInfo = requireContext().packageManager
            .getApplicationInfo(requireContext().packageName, PackageManager.GET_META_DATA)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_maps, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), appId)
        }
        val placesClient = Places.createClient(requireContext())

        val searchButton: Button = view.findViewById(R.id.btnSearch)
        searchButton.setOnClickListener {
            val placeFields = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, placeFields)
                .build(requireContext())
            startAutocomplete.launch(intent)
        }

        selectLocationButton = view.findViewById(R.id.btnSelectThisLocation)
        selectLocationButton.visibility = View.GONE
        selectLocationButton.setOnClickListener {
            // TODO: popup menu for three things: find LOI, check weather, (add to plan/calendar?)
            println("clicked")
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = false

        map.setOnMarkerClickListener(this)
        map.setOnMapClickListener(this)

        setUpMap()
    }

    private fun setUpMap() {
        if (requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requireActivity().requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        map.setOnMyLocationButtonClickListener(object : GoogleMap.OnMyLocationButtonClickListener {
            override fun onMyLocationButtonClick(): Boolean {
                val location = map.getMyLocation()
                val latlng = LatLng(location.latitude, location.longitude)
                markLocation(latlng)
                return false
            }
        })

        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (isAdded && location != null) {
                var currentLatLng = LatLng(location.latitude, location.longitude)
                if (currLatLng != null) {
                    currentLatLng = currLatLng as LatLng
                }
                markLocation(currentLatLng)
            }
        }
    }

    private fun getAddressFromLatLng(latlng: LatLng): String {
        val geocoder = Geocoder(requireActivity())
        val addressList: List<Address>?
        val address: Address?
        var addressStr = ""

        try {
            addressList = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                address = addressList[0]
                for (i: Int in 0..address.maxAddressLineIndex) {
                    if (i != 0) {
                        addressStr += '\n'
                    }
                    addressStr += address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return addressStr
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return false
    }

    override fun onMapClick(p0: LatLng) {
        markLocation(p0)
    }

    fun markLocation(latlng: LatLng) {
        val addrStr = getAddressFromLatLng(latlng)

        map.clear()
        map.addMarker(
            MarkerOptions().position(latlng).title(addrStr)
        )
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14f))
        currLatLng = latlng
        currAddress = addrStr
        selectLocationButton.visibility = View.VISIBLE
    }

    fun getMarkedAddress(): String {
        return currAddress
    }

    private val startAutocomplete =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val place = Autocomplete.getPlaceFromIntent(intent)
                    val lat = place.latLng?.latitude ?: defaultLatLng.latitude
                    val long = place.latLng?.longitude ?: defaultLatLng.longitude
                    markLocation(LatLng(lat, long))
                }
            } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
                // Handle Autocomplete error
                val status = Autocomplete.getStatusFromIntent(result.data)
                Log.e("MapsFragment", "Error during autocomplete: ${status.statusMessage}")
            }
        }
}