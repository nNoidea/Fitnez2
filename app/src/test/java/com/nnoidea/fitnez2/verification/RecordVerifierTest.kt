package com.nnoidea.fitnez2.verification

import org.junit.Assert.*
import org.junit.Test

class RecordVerifierTest {

    @Test
    fun validateSets_one_passes() {
        RecordVerifier.validateSets(1)
    }

    @Test
    fun validateSets_three_passes() {
        RecordVerifier.validateSets(3)
    }

    @Test
    fun validateSets_zero_throws() {
        try {
            RecordVerifier.validateSets(0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateSets_negative_throws() {
        try {
            RecordVerifier.validateSets(-1)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateReps_one_passes() {
        RecordVerifier.validateReps(1)
    }

    @Test
    fun validateReps_ten_passes() {
        RecordVerifier.validateReps(10)
    }

    @Test
    fun validateReps_zero_throws() {
        try {
            RecordVerifier.validateReps(0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateReps_negative_throws() {
        try {
            RecordVerifier.validateReps(-5)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateWeight_positive_passes() {
        RecordVerifier.validateWeight(20.0)
    }

    @Test
    fun validateWeight_zero_passes() {
        RecordVerifier.validateWeight(0.0)
    }

    @Test
    fun validateWeight_negative_passes() {
        RecordVerifier.validateWeight(-5.0)
    }

    @Test
    fun validateWeight_nan_throws() {
        try {
            RecordVerifier.validateWeight(Double.NaN)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateWeight_positiveInfinity_throws() {
        try {
            RecordVerifier.validateWeight(Double.POSITIVE_INFINITY)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateWeight_negativeInfinity_throws() {
        try {
            RecordVerifier.validateWeight(Double.NEGATIVE_INFINITY)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateRecord_allValid_passes() {
        RecordVerifier.validateRecord(3, 10, 20.0)
    }

    @Test
    fun validateRecord_invalidSets_throws() {
        try {
            RecordVerifier.validateRecord(0, 10, 20.0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateRecord_invalidReps_throws() {
        try {
            RecordVerifier.validateRecord(3, 0, 20.0)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateRecord_invalidWeight_throws() {
        try {
            RecordVerifier.validateRecord(3, 10, Double.NaN)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }
}
