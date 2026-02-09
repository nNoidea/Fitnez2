package com.nnoidea.fitnez2.core

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import com.nnoidea.fitnez2.core.localization.LocalizationManager
import java.lang.ref.WeakReference

object ValidateAndCorrect {

    private var appContext: WeakReference<Context>? = null

    fun init(context: Context) {
        appContext = WeakReference(context.applicationContext)
    }

    // Helper to show tooltip using Toast
    @Suppress("DEPRECATION") // Custom toast views are deprecated in API 30+ but necessary for gravity
    private fun showToast(message: String) {
        appContext?.get()?.let { context ->
            val toast = Toast(context)
            
            // Create a simple custom view programmatically
            val textView = TextView(context).apply {
                text = message
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setPadding(32, 24, 32, 24)
                
                // Rounded dark background
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(Color.parseColor("#333333"))
                    cornerRadius = 48f
                }
            }

            toast.view = textView
            toast.duration = Toast.LENGTH_SHORT
            toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 150) // 150 offset from top
            toast.show()
        }
    }

    /**
     * Validates and corrects Sets input.
     * Rules: Must be an integer > 0.
     * Handles inputs like "01" -> 1, "5.0" -> 5.
     * Returns the valid Integer or null if invalid.
     */
    fun sets(input: String): Int? {
        if (input.isBlank()) {
            showToast(LocalizationManager.strings.errorSetsEmpty)
            return null
        }
        val number = input.toDoubleOrNull()
        if (number == null) {
            showToast(LocalizationManager.strings.errorSetsFormat)
            return null
        }
        // Check if it's an integer (whole number)
        if (number % 1.0 != 0.0) {
            showToast(LocalizationManager.strings.errorSetsWholeNumber)
            return null
        }
        val intValue = number.toInt()
        if (intValue <= 0) {
            showToast(LocalizationManager.strings.errorSetsPositive)
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
            showToast(LocalizationManager.strings.errorRepsEmpty)
            return null
        }
        val number = input.toDoubleOrNull()
        if (number == null) {
            showToast(LocalizationManager.strings.errorRepsFormat)
            return null
        }
        if (number % 1.0 != 0.0) {
            showToast(LocalizationManager.strings.errorRepsWholeNumber)
            return null
        }
        val intValue = number.toInt()
        if (intValue <= 0) {
            showToast(LocalizationManager.strings.errorRepsPositive)
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
            showToast(LocalizationManager.strings.errorWeightEmpty)
            return null
        }
        val number = input.toDoubleOrNull()
        if (number == null) {
            showToast(LocalizationManager.strings.errorWeightFormat)
            return null
        }
        if (number.isNaN() || number.isInfinite()) {
           showToast(LocalizationManager.strings.errorWeightInvalid)
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
