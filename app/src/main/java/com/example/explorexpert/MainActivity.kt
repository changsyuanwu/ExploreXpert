package com.example.explorexpert

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.explorexpert.ui.theme.ExploreXpertTheme

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment;
        mapFragment.getMapAsync(this);
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.addMarker(
            MarkerOptions().position(LatLng(0.0, 0.0)).title("Marker")
        )
    }
}

//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ExploreXpertTheme {
//        Greeting("Android")
//    }
//}