package com.example.explorexpert

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import com.example.explorexpert.databinding.ActivityMainBinding
import com.example.explorexpert.ui.theme.ExploreXpertTheme
import com.example.explorexpert.ui.view.CalendarFragment
import com.example.explorexpert.ui.view.HomeFragment
import com.example.explorexpert.ui.view.MapsFragment
import com.example.explorexpert.ui.view.WeatherFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        swapFragment(HomeFragment())

        binding.bottomNav.setOnItemSelectedListener{
            when(it.itemId){
                R.id.nav_home -> swapFragment(HomeFragment())
                R.id.nav_calendar -> swapFragment(CalendarFragment())
                R.id.nav_map -> swapFragment(MapsFragment())
                R.id.nav_weather -> swapFragment(WeatherFragment())
                else -> {

                }
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