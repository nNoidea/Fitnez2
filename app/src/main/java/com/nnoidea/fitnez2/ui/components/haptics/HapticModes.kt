package com.nnoidea.fitnez2.ui.components.haptics

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * All available haptic feedback modes for threshold POPs when gestures cross a commitment point
 * (such as Swipe-to-Delete threshold or BottomSheet expansion midpoint).
 */
enum class PopHapticMode(val displayName: String) {
    SPRING_SNAP("Spring Snap (Quick Rise + Click)"),
    PRIMITIVE_THUD("Hardware Thud (Deep LRA Punch)"),
    LINEAGE_COMPOUND("Lineage SystemUI (Tick + Click)"),
    PRIMITIVE_CLICK("Hardware Crisp Click"),
    DOUBLE_PRIMITIVE_CLICK("Hardware Double Click"),
    EFFECT_HEAVY_CLICK("M3 Heavy Click (Prebaked 5)"),
    EFFECT_POP("M3 Prebaked Pop (Prebaked 4)"),
    STRONG_POP("Actuator Punch (Customizable ms)"),
    DOUBLE_PULSE("Double Motor Pulse"),
    GESTURE_THRESHOLD("Android 14+ Gesture Threshold"),
    CONFIRM("System Confirm"),
    REJECT("System Reject"),
    LONG_PRESS("System Long Press"),
    NONE("None (Silent)");

    override fun toString(): String = displayName
}

/**
 * All available haptic modes for continuous slider ticks felt while dragging horizontally or vertically.
 */
enum class SliderTickMode(val displayName: String) {
    PRIMITIVE_LOW_TICK("Hardware Low Tick (Ultra-Crisp LRA)"),
    PRIMITIVE_TICK("Hardware Tick (Crisp LRA Detent)"),
    LINEAGE_SLIDER_TICK("LineageOS Slider (System UI Code 23)"),
    PRIMITIVE_SPIN("Hardware Spin Detent"),
    SEGMENT_TICK("Android 14+ Segment Tick"),
    SEGMENT_FREQUENT_TICK("Android 14+ Frequent Tick"),
    EFFECT_TICK("M3 Prebaked Tick (EFFECT_TICK)"),
    MICRO_PULSE("Actuator Micro Pulse (10ms)"),
    CLOCK_TICK("System Clock Tick"),
    NONE("None (Silent)");

    override fun toString(): String = displayName
}

/**
 * Locked-in standard defaults for modern Android 12+ (API 31+) tactile feel.
 */
object HapticDefaults {
    /** High-frequency micro-notch distance (2dp) */
    val TickIntervalDp: Dp = 2.dp

    /** Whisper-light detent power (20%) */
    const val SwipeTickScale: Float = 0.20f

    /** High-contrast punch for threshold crossing (100%) */
    const val PopScale: Float = 1.0f

    /** Card deletion threshold (35% of card width) */
    const val SwipeDeleteThresholdFraction: Float = 0.35f

    /** Minimum duration between LRA ticks to allow reverse-phase DSP active braking */
    const val MinimumTickIntervalMs: Long = 16L

    /** Default POP mode: mechanical spring tension release */
    val DefaultPopMode: PopHapticMode = PopHapticMode.SPRING_SNAP

    /** Default tick mode: ultra-crisp active-braked hardware low tick */
    val DefaultSliderTickMode: SliderTickMode = SliderTickMode.PRIMITIVE_LOW_TICK
}
