package com.nnoidea.fitnez2.verification

import com.nnoidea.fitnez2.core.localization.globalLocalization

object WorkoutVerifier {

    fun validateName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            throw IllegalArgumentException(globalLocalization.errorWorkoutNameBlank)
        }
        return trimmed
    }

    fun validateWorkoutRecord(sets: Int, reps: Int, weight: Double) {
        if (sets <= 0) {
            throw IllegalArgumentException(globalLocalization.errorSetsPositive)
        }
        if (reps <= 0) {
            throw IllegalArgumentException(globalLocalization.errorRepsPositive)
        }
        if (weight.isNaN() || weight.isInfinite()) {
            throw IllegalArgumentException(globalLocalization.errorWeightInvalid)
        }
    }
}
