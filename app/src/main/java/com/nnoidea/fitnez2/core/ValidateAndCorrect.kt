package com.nnoidea.fitnez2.core

import android.content.Context
import android.widget.Toast
import com.nnoidea.fitnez2.core.localization.LocalizationManager

object ValidateAndCorrect {

    /** Set once from ProvideGlobalUiState. */
    var appContext: Context? = null

    private fun showError(message: String) {
        appContext?.let { Toast.makeText(it, message, Toast.LENGTH_SHORT).show() }
    }

    /**
     * Shared validation for positive integer inputs (sets, reps).
     * Rules: Must be an integer > 0.
     * Handles inputs like "01" -> 1, "5.0" -> 5.
     */
    private fun positiveInt(
        input: String,
        errorEmpty: String,
        errorFormat: String,
        errorWholeNumber: String,
        errorPositive: String
    ): Int? {
        if (input.isBlank()) { showError(errorEmpty); return null }
        val number = input.toDoubleOrNull()
            ?: run { showError(errorFormat); return null }
        if (number % 1.0 != 0.0) { showError(errorWholeNumber); return null }
        val intValue = number.toInt()
        if (intValue <= 0) { showError(errorPositive); return null }
        return intValue
    }

    /** Validates and corrects Sets input. Returns the valid Integer or null. */
    fun sets(input: String): Int? = positiveInt(
        input,
        LocalizationManager.strings.errorSetsEmpty,
        LocalizationManager.strings.errorSetsFormat,
        LocalizationManager.strings.errorSetsWholeNumber,
        LocalizationManager.strings.errorSetsPositive
    )

    /** Validates and corrects Reps input. Returns the valid Integer or null. */
    fun reps(input: String): Int? = positiveInt(
        input,
        LocalizationManager.strings.errorRepsEmpty,
        LocalizationManager.strings.errorRepsFormat,
        LocalizationManager.strings.errorRepsWholeNumber,
        LocalizationManager.strings.errorRepsPositive
    )

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
