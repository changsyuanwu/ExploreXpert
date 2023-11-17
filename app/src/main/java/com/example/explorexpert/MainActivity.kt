package com.example.explorexpert

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
}