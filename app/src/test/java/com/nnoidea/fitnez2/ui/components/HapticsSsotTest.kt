package com.nnoidea.fitnez2.ui.components

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nnoidea.fitnez2.ui.components.haptics.HapticEngine
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HapticsSsotTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testHapticEngine_methodsExecuteSafely() {
        HapticEngine.performClick(context, 0.60f)
        HapticEngine.performSpringSnap(context, 1.0f)
        HapticEngine.performClockBack(context, 0.20f)
        HapticEngine.performConfirm(null)
        HapticEngine.performReject(null)
    }
}
