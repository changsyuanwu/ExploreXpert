package com.example.explorexpert

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.explorexpert.databinding.ActivityMainBinding
import com.example.explorexpert.ui.view.CalendarFragment
import com.example.explorexpert.ui.view.HomeFragment
import com.example.explorexpert.ui.view.MapsFragment
import com.example.explorexpert.ui.view.PlanFragment
import dagger.hilt.android.AndroidEntryPoint
import com.example.explorexpert.ui.view.WeatherFragment
import com.google.android.libraries.places.api.Places

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var homeFragment: HomeFragment;
    private lateinit var calendarFragment: CalendarFragment;
    private lateinit var mapFragment: MapsFragment;
    private lateinit var weatherFragment: WeatherFragment;
    private lateinit var planFragment: PlanFragment;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Initialize Google Places
        val appInfo = applicationContext.packageManager
            .getApplicationInfo(applicationContext.packageName, PackageManager.GET_META_DATA)
        val appId = appInfo.metaData?.getString("com.google.android.geo.API_KEY")

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, appId);
        }
        val placesClient = Places.createClient(applicationContext)

        homeFragment = HomeFragment();
        calendarFragment = CalendarFragment();
        mapFragment = MapsFragment();
        weatherFragment = WeatherFragment();
        planFragment = PlanFragment();

        setContentView(binding.root)
        swapFragment(homeFragment)

        binding.bottomNav.setOnItemSelectedListener{
            when(it.itemId){
                R.id.nav_home -> swapFragment(homeFragment)
                R.id.nav_calendar -> swapFragment(calendarFragment)
                R.id.nav_map -> swapFragment(mapFragment);
                R.id.nav_weather -> swapFragment(weatherFragment)
                R.id.nav_plan -> swapFragment(planFragment)
                else -> false
            }
            true
        }


    }

    // function to swap fragments in the fragment container
    private fun swapFragment(fragment: Fragment){
        // get fragment manager from current activity
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.container, fragment)?.commit()
    }

    fun getMapFragment(): MapsFragment {
        return mapFragment;
    }

    fun swapToPlanFragment() {
        swapFragment(planFragment);
        binding.bottomNav.selectedItemId = R.id.nav_plan
    }

    fun swapToWeatherFragment() {
        swapFragment(weatherFragment);
        binding.bottomNav.selectedItemId = R.id.nav_weather
    }
}