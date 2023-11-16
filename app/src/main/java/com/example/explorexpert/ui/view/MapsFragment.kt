package com.example.explorexpert.ui.view

import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
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
import java.io.IOException

class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback,
                        GoogleMap.OnMarkerClickListener {
    private var currLatLng: LatLng? = null

    private lateinit var searchView: SearchView
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_maps, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchView = view.findViewById(R.id.mapSearchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val geocoder = Geocoder(requireActivity())
                val location: String = searchView.query.toString()

                // TODO: really similar to getAddressFromLatLng(), fix later
                try {
                    val addressList = geocoder.getFromLocationName(location, 1)
                    if (addressList != null && addressList.isNotEmpty()) {
                        val address = addressList[0]
                        val latlng = LatLng(address.latitude, address.longitude)
                        val addrStr = getAddressFromLatLng(latlng)

                        map.clear()
                        map.addMarker(
                            MarkerOptions().position(latlng).title(addrStr)
                        )
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12f))
                        currLatLng = latlng
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true
        map.setOnMarkerClickListener(this)

        setUpMap()
    }

    private fun setUpMap() {
        if (requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requireActivity().requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
            if (isAdded && location != null) {
                var currentLatLng = LatLng(location.latitude, location.longitude)
                if (currLatLng != null) {
                    currentLatLng = currLatLng as LatLng
                } else {
                    currLatLng = currentLatLng
                }

                val currAddrStr = getAddressFromLatLng(currentLatLng)
                map.addMarker(MarkerOptions().position(currentLatLng).title(currAddrStr))
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // TODO: implement this
    }
}