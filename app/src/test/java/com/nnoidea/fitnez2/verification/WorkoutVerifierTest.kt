package com.nnoidea.fitnez2.verification

import org.junit.Assert.*
import org.junit.Test

class WorkoutVerifierTest {

    @Test
    fun validateName_validName_returnsTrimmed() {
        val result = WorkoutVerifier.validateName("Leg Day")
        assertEquals("Leg Day", result)
    }

    @Test
    fun validateName_whitespace_trimmed() {
        val result = WorkoutVerifier.validateName("  Push Day  ")
        assertEquals("Push Day", result)
    }

    @Test
    fun validateName_blank_throws() {
        try {
            WorkoutVerifier.validateName("")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateName_whitespaceOnly_throws() {
        try {
            WorkoutVerifier.validateName("   ")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateWorkoutRecord_allValid_passes() {
        WorkoutVerifier.validateWorkoutRecord(3, 10, 50.0)
    }

    @Test
    fun validateWorkoutRecord_invalidSets_throws() {
        try {
            WorkoutVerifier.validateWorkoutRecord(0, 10, 50.0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateWorkoutRecord_invalidReps_throws() {
        try {
            WorkoutVerifier.validateWorkoutRecord(3, -1, 50.0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateWorkoutRecord_nanWeight_throws() {
        try {
            WorkoutVerifier.validateWorkoutRecord(3, 10, Double.NaN)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateWorkoutRecord_infinityWeight_throws() {
        try {
            WorkoutVerifier.validateWorkoutRecord(3, 10, Double.POSITIVE_INFINITY)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }
}
