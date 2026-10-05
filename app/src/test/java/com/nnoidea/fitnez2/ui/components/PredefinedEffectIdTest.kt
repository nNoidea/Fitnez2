package com.nnoidea.fitnez2.ui.components

import android.os.VibrationEffect
import com.nnoidea.fitnez2.ui.components.haptics.PopHapticMode
import com.nnoidea.fitnez2.ui.components.haptics.SliderTickMode
import com.nnoidea.fitnez2.ui.components.haptics.predefinedEffectId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Guards that no prebaked effect reaches VibrationEffect.createPredefined with an id the
 * platform does not define. EFFECT_POP is @hide in AOSP and has no public constant, so
 * it must never be passed as a literal; lint (WrongConstant) flags exactly this.
 */
class PredefinedEffectIdTest {

    @Test
    fun everyResolvedId_isAPublicPredefinedConstant() {
        val publicIds = setOf(
            VibrationEffect.EFFECT_CLICK,
            VibrationEffect.EFFECT_DOUBLE_CLICK,
            VibrationEffect.EFFECT_HEAVY_CLICK,
            VibrationEffect.EFFECT_TICK
        )
        PopHapticMode.entries.forEach { mode ->
            val id = predefinedEffectId(mode) ?: return@forEach
            assertEquals("PopHapticMode.$mode resolves to non-public id $id", true, id in publicIds)
        }
        SliderTickMode.entries.forEach { mode ->
            val id = predefinedEffectId(mode) ?: return@forEach
            assertEquals("SliderTickMode.$mode resolves to non-public id $id", true, id in publicIds)
        }
    }

    @Test
    fun effectPop_degradesToHeavyClick_ratherThanUsingHiddenConstant() {
        assertEquals(VibrationEffect.EFFECT_HEAVY_CLICK, predefinedEffectId(PopHapticMode.EFFECT_POP))
    }

    @Test
    fun effectHeavyClick_isIdentity() {
        assertEquals(
            VibrationEffect.EFFECT_HEAVY_CLICK,
            predefinedEffectId(PopHapticMode.EFFECT_HEAVY_CLICK)
        )
    }

    @Test
    fun effectTick_isIdentity() {
        assertEquals(VibrationEffect.EFFECT_TICK, predefinedEffectId(SliderTickMode.EFFECT_TICK))
    }

    @Test
    fun nonPredefinedModes_returnNull() {
        assertNull(predefinedEffectId(PopHapticMode.SPRING_SNAP))
        assertNull(predefinedEffectId(PopHapticMode.PRIMITIVE_THUD))
        assertNull(predefinedEffectId(PopHapticMode.NONE))
        assertNull(predefinedEffectId(SliderTickMode.PRIMITIVE_LOW_TICK))
        assertNull(predefinedEffectId(SliderTickMode.NONE))
    }
}
