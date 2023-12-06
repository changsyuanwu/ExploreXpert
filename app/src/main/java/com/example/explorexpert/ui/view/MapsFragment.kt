package com.example.explorexpert.ui.view

import android.app.Activity
import android.content.DialogInterface
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
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.explorexpert.R
import com.google.android.gms.common.api.ApiException
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
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback,
                        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {
    private var currLatLng: LatLng? = null
    private var currAddress: String = ""
    private var currPrimaryAddr: String = ""
    private var currSecondaryAddr: String = ""
    private var currPlaceID: String = ""

    private var nearbyPlacesNames: ArrayList<String> = arrayListOf()
    private var nearbyPlacesLatLngs: ArrayList<LatLng> = arrayListOf()

    private var defaultLatLng = LatLng(43.4723, -80.5449)

    private lateinit var appInfo: ApplicationInfo
    private lateinit var map: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchButton: Button
    private lateinit var selectLocationButton: Button
    private lateinit var view: View

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private val TAG = "MapsFragment"
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
        view = inflater.inflate(R.layout.fragment_maps, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), appId)
        }

        val startAutocomplete =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    if (intent != null) {
                        val place = Autocomplete.getPlaceFromIntent(intent)
                        generatePlace(place.latLng ?: defaultLatLng)
                    }
                } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
                    val status = Autocomplete.getStatusFromIntent(result.data)
                    Log.e(TAG, "Error during autocomplete: ${status.statusMessage}")
                }
            }

        searchButton = view.findViewById(R.id.btnSearch)
        searchButton.setOnClickListener {
            val placeFields = listOf(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, placeFields)
                .build(requireContext())
            startAutocomplete.launch(intent)
        }

        selectLocationButton = view.findViewById(R.id.btnSelectThisLocation)
        selectLocationButton.visibility = View.GONE
        selectLocationButton.setOnClickListener {
            val locationBottomSheetDialogFragment = LocationBottomSheetDialogFragment()
            locationBottomSheetDialogFragment.show(
                childFragmentManager,
                LocationBottomSheetDialogFragment.TAG
            )
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
            generatePlace(currLatLng ?: defaultLatLng)
            return
        }

        map.isMyLocationEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true

        map.setOnMyLocationButtonClickListener(object : GoogleMap.OnMyLocationButtonClickListener {
            override fun onMyLocationButtonClick(): Boolean {
                try {
                    val locationResult = fusedLocationClient.lastLocation
                    if (isAdded) {
                        locationResult.addOnCompleteListener(requireActivity()) { task ->
                            if (task.isSuccessful) {
                                val currLoc = task.result
                                if (currLoc != null) {
                                    generatePlace(LatLng(currLoc.latitude, currLoc.longitude))
                                } else {
                                    Log.e(TAG, "Exception: %s", task.exception)
                                }
                            }
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, e.message, e)
                }
                return false
            }
        })

        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (isAdded && location != null) {
                var currentLatLng = LatLng(location.latitude, location.longitude)
                if (currLatLng != null) {
                    currentLatLng = currLatLng as LatLng
                }
                generatePlace(currentLatLng)
            }
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return false
    }

    override fun onMapClick(p0: LatLng) {
        generatePlace(p0)
    }

    private fun generatePlace(latlng: LatLng) {
        // Use Geocoder for an approximate address
        val approxAddr = getAddressFromLatLng(latlng)

        // Use Places Autocomplete to generate a place and more specific address
        getPlaceFromAddress(approxAddr, latlng)
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

    fun getPlaceFromAddress(addr: String, latlng: LatLng) {
        val request = FindAutocompletePredictionsRequest.builder().setQuery(addr).build()
        val placesClient = Places.createClient(requireContext())
        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
            if (response.autocompletePredictions.size == 0) {
                currAddress = ""
                currPrimaryAddr = ""
                currSecondaryAddr = ""
                currPlaceID = ""
            } else {
                // Use best result
                currAddress = response.autocompletePredictions[0].getFullText(null).toString()
                currPrimaryAddr = response.autocompletePredictions[0].getPrimaryText(null).toString()
                currSecondaryAddr = response.autocompletePredictions[0].getSecondaryText(null).toString()
                currPlaceID = response.autocompletePredictions[0].placeId
            }
            markLocation(latlng)
        }.addOnFailureListener { exception: Exception? ->
            if (exception is ApiException) {
                Log.e(TAG, "${exception.statusCode}")
            }
        }
    }

    private fun markLocation(latlng: LatLng) {
        map.clear()
        map.addMarker(
            MarkerOptions().position(latlng).title(currPrimaryAddr).snippet(currSecondaryAddr)
        )
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14f))
        currLatLng = latlng
        selectLocationButton.visibility = if (currPlaceID == "") { View.GONE } else { View.VISIBLE }
    }

    private fun suggestNearbyLocations() {
        if (nearbyPlacesNames.size != 0) {
            val listener = DialogInterface.OnClickListener { dialog, which ->
                val selectedLatLng = nearbyPlacesLatLngs[which]
                generatePlace(selectedLatLng)
            }
            val cs: Array<CharSequence> = nearbyPlacesNames.toArray(arrayOfNulls<CharSequence>(nearbyPlacesNames.size))

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Pick a Suggestion")
                .setItems(cs, listener)
                .show()
        } else {
            Snackbar.make(view, "No suggestions available.", Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    fun getNearbyLocations() {
        nearbyPlacesNames.clear()
        nearbyPlacesLatLngs.clear()

        val apiKey = appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        val nearbySearchReq = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=${(currLatLng as LatLng).latitude}%2C${(currLatLng as LatLng).longitude}" +
                "&radius=5000" +
                "&type=point_of_interest" +
                "&key=$apiKey"

        val queue = Volley.newRequestQueue(requireContext())

        var collected = 0

        val stringReq = StringRequest(
            Request.Method.GET, nearbySearchReq,
            Response.Listener<String> { response ->
                try {
                    val obj = JSONObject(response)
                    val results = obj.getJSONArray("results")

                    for (i in 0 until results.length()) {
                        val currResult = results.getJSONObject(i)
                        val location = currResult.getJSONObject("geometry")
                            .getJSONObject("location")
                        val latitude = location.getDouble("lat")
                        val longitude = location.getDouble("lng")

                        val name = currResult.getString("name")

                        if (name != "") {
                            nearbyPlacesNames.add(name)
                            nearbyPlacesLatLngs.add(LatLng(latitude, longitude))
                            collected++

                            if (collected >= 5) break
                        }
                    }
                    suggestNearbyLocations()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Error: ${error.message}")
            })

        queue.add(stringReq)
    }

    fun getCurrPlaceID(): String {
        return currPlaceID
    }

    fun getCurrLatLng(): LatLng {
        return currLatLng ?: defaultLatLng
    }

    fun getMapFragment(): SupportMapFragment {
        return mapFragment
    }

    fun getPrimaryAddr(): String {
        return currPrimaryAddr
    }

    fun getSecondaryAddr(): String {
        return currSecondaryAddr
    }
}