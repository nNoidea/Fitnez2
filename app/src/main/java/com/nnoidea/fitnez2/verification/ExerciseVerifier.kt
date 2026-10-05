package com.nnoidea.fitnez2.verification

import com.nnoidea.fitnez2.core.localization.globalLocalization

object ExerciseVerifier {

    fun validateName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            throw IllegalArgumentException(globalLocalization.errorExerciseNameBlank)
        }
        return trimmed
    }
}
