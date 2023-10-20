package com.example.explorexpert

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle

import androidx.fragment.app.commit;
import androidx.fragment.app.add;

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        val manager = supportFragmentManager;

        if (savedInstanceState == null) {
            manager.commit {
                setReorderingAllowed(true);
                add<MapsFragment>(R.id.map_fragment);
            }
        }
    }

}
