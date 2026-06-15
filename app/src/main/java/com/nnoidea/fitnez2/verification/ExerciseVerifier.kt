package com.nnoidea.fitnez2.verification

import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Exercise

object ExerciseVerifier {

    fun validateName(name: String): String {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            throw IllegalArgumentException(globalLocalization.errorExerciseNameBlank)
        }
        return trimmed
    }

    fun validateIdNotSet(id: String) {
        if (id.isNotEmpty()) {
            throw IllegalArgumentException(globalLocalization.errorIdMustBeZero)
        }
    }

    suspend fun validateNotDuplicate(name: String, lookup: suspend (String) -> Exercise?) {
        val existing = lookup(name)
        if (existing != null) {
            throw IllegalArgumentException(globalLocalization.errorExerciseAlreadyExists(name))
        }
    }

    suspend fun validateUpdateAllowed(existingId: String, newName: String, lookup: suspend (String) -> Exercise?) {
        val existing = lookup(newName)
        if (existing != null && existing.id != existingId) {
            throw IllegalArgumentException(globalLocalization.errorExerciseRenameConflict(newName))
        }
    }
}
