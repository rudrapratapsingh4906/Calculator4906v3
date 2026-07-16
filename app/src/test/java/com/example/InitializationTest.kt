package com.example

import androidx.test.core.app.ActivityScenario
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import androidx.lifecycle.Lifecycle

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [24]) // Try lower API level where desugaring or other issues might happen
class InitializationTest {

    @Test
    fun testInitialization() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.moveToState(Lifecycle.State.RESUMED)
    }
}
