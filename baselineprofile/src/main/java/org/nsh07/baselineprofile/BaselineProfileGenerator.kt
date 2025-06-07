package org.nsh07.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // The application id for the running build variant is read from the instrumentation arguments.
        rule.collect(
            packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),

            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()
            device.wait(Until.hasObject(By.text("Featured article")), 5000)

            device.findObject(By.text("Search Wikipedia")).click()
            Thread.sleep(1000)
            device.pressBack()
            device.pressBack()

            device.findObject(By.descContains("language")).click()
            Thread.sleep(1000)
            device.pressBack()

            navigateTo("Saved", device)
            navigateTo("Settings", device)
            navigateTo("About", device)
        }
    }

    fun navigateTo(text: String, device: UiDevice) {
        device.findObject(By.desc("More options")).click()
        Thread.sleep(1000)

        device.findObject(By.text(text)).click()
        Thread.sleep(1000)
        device.findObject(By.desc("Go back")).click()
        Thread.sleep(1000)
    }
}