package com.example.explorexpert

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle

import androidx.fragment.app.commit;
import androidx.fragment.app.add;

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
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

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
