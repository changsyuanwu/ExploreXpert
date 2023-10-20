package com.example.explorexpert

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Hide the status bar and make the splash screen as a full screen activity.
        window.decorView.windowInsetsController?.hide(
            WindowInsets.Type.statusBars()
        )

        auth = FirebaseAuth.getInstance()

        val isAuthenticated = checkAuthStatus()

        // Launch the appropriate activity based on auth status
        val targetActivityClass = if (isAuthenticated) {
            MainActivity::class.java
        } else {
            AuthActivity::class.java
        }

        val intent = Intent(this, targetActivityClass)
        startActivity(intent)
        finish()
    }

    private fun checkAuthStatus(): Boolean {
        return false
//        return auth.currentUser != null
    }
}