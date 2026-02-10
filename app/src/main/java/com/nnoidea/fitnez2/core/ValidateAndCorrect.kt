package com.nnoidea.fitnez2.core

import com.nnoidea.fitnez2.core.localization.LocalizationManager
import com.nnoidea.fitnez2.ui.common.GlobalUiState

object ValidateAndCorrect {

    private fun showError(message: String) {
        GlobalUiState.instance?.showSnackbar(message)
    }

    /**
     * Validates and corrects Sets input.
     * Rules: Must be an integer > 0.
     * Handles inputs like "01" -> 1, "5.0" -> 5.
     * Returns the valid Integer or null if invalid.
     */
    fun sets(input: String): Int? {
        if (input.isBlank()) {
            showError(LocalizationManager.strings.errorSetsEmpty)
            return null
        }
        val number = input.toDoubleOrNull()
        if (number == null) {
            showError(LocalizationManager.strings.errorSetsFormat)
            return null
        }
        if (number % 1.0 != 0.0) {
            showError(LocalizationManager.strings.errorSetsWholeNumber)
            return null
        }
        val intValue = number.toInt()
        if (intValue <= 0) {
            showError(LocalizationManager.strings.errorSetsPositive)
            return null
        }
        return intValue
    }

    /**
     * Validates and corrects Reps input.
     * Rules: Must be an integer > 0.
     * Returns the valid Integer or null if invalid.
     */
    fun reps(input: String): Int? {
        if (input.isBlank()) {
            showError(LocalizationManager.strings.errorRepsEmpty)
            return null
        }
        val number = input.toDoubleOrNull()
        if (number == null) {
            showError(LocalizationManager.strings.errorRepsFormat)
            return null
        }
        if (number % 1.0 != 0.0) {
            showError(LocalizationManager.strings.errorRepsWholeNumber)
            return null
        }
        val intValue = number.toInt()
        if (intValue <= 0) {
            showError(LocalizationManager.strings.errorRepsPositive)
            return null
        }
        return intValue
    }

    /**
     * Validates and corrects Weight input.
     * Rules: Must be a valid number. (Negative allowed per user spec "can be negative or positive")
     * Returns the valid Double or null if invalid.
     */
    fun weight(input: String): Double? {
        if (input.isBlank()) {
            showError(LocalizationManager.strings.errorWeightEmpty)
            return null
        }
        val number = input.toDoubleOrNull()
        if (number == null) {
            showError(LocalizationManager.strings.errorWeightFormat)
            return null
        }
        if (number.isNaN() || number.isInfinite()) {
           showError(LocalizationManager.strings.errorWeightInvalid)
           return null
        }
        return number
    }

    // Direct value validation for DAO / Internal use

    fun validateSets(value: Int): Boolean {
        return value > 0
    }

    fun validateReps(value: Int): Boolean {
        return value > 0
    }

    fun validateWeight(value: Double): Boolean {
        return !value.isNaN() && !value.isInfinite()
    }
}
