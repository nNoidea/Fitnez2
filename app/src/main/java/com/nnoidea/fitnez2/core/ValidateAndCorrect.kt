package com.nnoidea.fitnez2.core

object ValidateAndCorrect {

    /**
     * Validates and corrects Sets input.
     * Rules: Must be an integer > 0.
     * Handles inputs like "01" -> 1, "5.0" -> 5.
     * Returns the valid Integer or null if invalid.
     */
    fun sets(input: String): Int? {
        val number = input.toDoubleOrNull() ?: return null
        // Check if it's an integer (whole number)
        if (number % 1.0 != 0.0) return null
        val intValue = number.toInt()
        return if (intValue > 0) intValue else null
    }

    /**
     * Validates and corrects Reps input.
     * Rules: Must be an integer > 0.
     * Returns the valid Integer or null if invalid.
     */
    fun reps(input: String): Int? {
        val number = input.toDoubleOrNull() ?: return null
        if (number % 1.0 != 0.0) return null
        val intValue = number.toInt()
        return if (intValue > 0) intValue else null
    }

    /**
     * Validates and corrects Weight input.
     * Rules: Must be a valid number. (Negative allowed per user spec "can be negative or positive")
     * Returns the valid Double or null if invalid.
     */
    fun weight(input: String): Double? {
        return input.toDoubleOrNull()
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
