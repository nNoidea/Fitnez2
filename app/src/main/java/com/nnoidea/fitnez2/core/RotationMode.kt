package com.nnoidea.fitnez2.core

/**
 * Single source of truth for rotation mode values used in settings and orientation logic.
 */
object RotationMode {
    const val SYSTEM = "system"
    const val ON = "on"
    const val OFF = "off"

    val ALL = listOf(SYSTEM, ON, OFF)
}
