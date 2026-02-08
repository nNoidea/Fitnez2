package com.nnoidea.fitnez2.core

import com.nnoidea.fitnez2.ui.common.GlobalUiState

object ValidateAndCorrect {

    // Helper to show tooltip using global instance
    private fun showTooltip(message: String) {
        GlobalUiState.instance?.showTooltip(message)
    }

    /**
     * Validates and corrects Sets input.
     * Rules: Must be an integer > 0.
     * Handles inputs like "01" -> 1, "5.0" -> 5.
     * Returns the valid Integer or null if invalid.
     */
    fun sets(input: String): Int? {
        if (input.isBlank()) {
            showTooltip("Sets cannot be empty")
            return null
        }
        val number = input.toDoubleOrNull()
        if (number == null) {
            showTooltip("Invalid sets format")
            return null
        }
        // Check if it's an integer (whole number)
        if (number % 1.0 != 0.0) {
            showTooltip("Sets must be a whole number")
            return null
        }
        val intValue = number.toInt()
        if (intValue <= 0) {
            showTooltip("Sets must be greater than 0")
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
            showTooltip("Reps cannot be empty")
            return null
        }
        val number = input.toDoubleOrNull()
        if (number == null) {
            showTooltip("Invalid reps format")
            return null
        }
        if (number % 1.0 != 0.0) {
            showTooltip("Reps must be a whole number")
            return null
        }
        val intValue = number.toInt()
        if (intValue <= 0) {
            showTooltip("Reps must be greater than 0")
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
            showTooltip("Weight cannot be empty")
            return null
        }
        val number = input.toDoubleOrNull()
        if (number == null) {
            showTooltip("Invalid weight format")
            return null
        }
        if (number.isNaN() || number.isInfinite()) {
           showTooltip("Invalid weight value")
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
        // Double can be NaN or Infinite, we probably want to assume finite
        return !value.isNaN() && !value.isInfinite()
    }
}
