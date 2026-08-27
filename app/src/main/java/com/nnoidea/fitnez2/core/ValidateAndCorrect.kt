package com.nnoidea.fitnez2.core

object ValidateAndCorrect {

    /**
     * Shared validation for positive integer inputs (sets, reps).
     * Rules: Must be an integer > 0.
     * Handles inputs like "01" -> 1, "5.0" -> 5.
     */
    private fun positiveInt(input: String): Int? {
        if (input.isBlank()) return null
        val number = input.toDoubleOrNull() ?: return null
        if (number % 1.0 != 0.0) return null
        val intValue = number.toInt()
        if (intValue <= 0) return null
        return intValue
    }

    /** Validates and corrects Sets input. Returns the valid Integer or null. */
    fun sets(input: String): Int? = positiveInt(input)

    /** Validates and corrects Reps input. Returns the valid Integer or null. */
    fun reps(input: String): Int? = positiveInt(input)

    /**
     * Validates and corrects Weight input.
     * Rules: Must be a valid number. (Negative allowed per user spec "can be negative or positive")
     * Returns the valid Double or null if invalid.
     */
    fun weight(input: String): Double? {
        if (input.isBlank()) {
            return null
        }
        val number = input.toDoubleOrNull() ?: return null
        if (number.isNaN() || number.isInfinite()) {
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
