package com.example.explorexpert

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.explorexpert.ui.view.MapsFragment
import com.google.android.gms.maps.model.LatLng

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import java.util.Timer
import kotlin.concurrent.timerTask

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MapsFragmentTest {

    private lateinit var scenario: FragmentScenario<MapsFragment>

    @Before
    fun setup() {
        scenario = launchFragmentInContainer<MapsFragment>()
        scenario.moveToState(Lifecycle.State.STARTED)
    }

    @Test
    fun queryDefaultAddress() {
        scenario.onFragment { fragment ->
            fragment.getPlaceFromAddress("University of Waterloo", LatLng(43.4723, -80.5449))

            // Give time for response to complete
            val timer = Timer()
            timer.schedule(
                timerTask {
                    assertEquals("200 University Ave W", fragment.getPrimaryAddr())
                    assertEquals("Waterloo, ON N2L 3G1, Canada", fragment.getSecondaryAddr())
                    timer.cancel()
                },
                2000
            )
        }
    }

    @Test
    fun testFragmentLaunch() {
        scenario.onFragment { fragment ->
            assertNotEquals(null, fragment.getMapFragment())
        }
    }
}