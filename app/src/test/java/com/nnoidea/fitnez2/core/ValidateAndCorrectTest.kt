package com.nnoidea.fitnez2.core

import org.junit.Test
import org.junit.Assert.*

class ValidateAndCorrectTest {

    @Test
    fun validateSets() {
        // Valid cases
        assertEquals(1, ValidateAndCorrect.sets("1"))
        assertEquals(1, ValidateAndCorrect.sets("01"))
        assertEquals(5, ValidateAndCorrect.sets("5.0"))
        assertEquals(5, ValidateAndCorrect.sets("5.000"))

        // Invalid cases
        assertNull(ValidateAndCorrect.sets("-5"))
        assertNull(ValidateAndCorrect.sets("0")) // Assuming > 0 requirement
        assertNull(ValidateAndCorrect.sets("1.5"))
        assertNull(ValidateAndCorrect.sets("abc"))
    }

    @Test
    fun validateReps() {
        // Valid cases
        assertEquals(10, ValidateAndCorrect.reps("10"))
        assertEquals(10, ValidateAndCorrect.reps("010"))
        assertEquals(10, ValidateAndCorrect.reps("10.0"))

        // Invalid cases
        assertNull(ValidateAndCorrect.reps("-1"))
        assertNull(ValidateAndCorrect.reps("0"))
        assertNull(ValidateAndCorrect.reps("abc"))
    }

    @Test
    fun validateWeight() {
        // Valid cases
        assertEquals(20.0, ValidateAndCorrect.weight("20")!!, 0.0)
        assertEquals(20.0, ValidateAndCorrect.weight("20.0")!!, 0.0)
        assertEquals(20.5, ValidateAndCorrect.weight("20.5")!!, 0.0)
        assertEquals(-5.0, ValidateAndCorrect.weight("-5")!!, 0.0) // Negative allowed

        // Invalid cases
        assertNull(ValidateAndCorrect.weight("abc"))
    }

    @Test
    fun sets_emptyString_returnsNull() {
        assertNull(ValidateAndCorrect.sets(""))
    }

    @Test
    fun sets_blankString_returnsNull() {
        assertNull(ValidateAndCorrect.sets("   "))
    }

    @Test
    fun reps_emptyString_returnsNull() {
        assertNull(ValidateAndCorrect.reps(""))
    }

    @Test
    fun weight_emptyString_returnsNull() {
        assertNull(ValidateAndCorrect.weight(""))
    }

    @Test
    fun weight_NaN_returnsNull() {
        assertNull(ValidateAndCorrect.weight("NaN"))
    }

    @Test
    fun weight_Infinity_returnsNull() {
        assertNull(ValidateAndCorrect.weight("Infinity"))
    }

    @Test
    fun validateSets_1_returnsTrue() {
        assertTrue(ValidateAndCorrect.validateSets(1))
    }

    @Test
    fun validateSets_0_returnsFalse() {
        assertFalse(ValidateAndCorrect.validateSets(0))
    }

    @Test
    fun validateSets_negative1_returnsFalse() {
        assertFalse(ValidateAndCorrect.validateSets(-1))
    }

    @Test
    fun validateReps_1_returnsTrue() {
        assertTrue(ValidateAndCorrect.validateReps(1))
    }

    @Test
    fun validateReps_0_returnsFalse() {
        assertFalse(ValidateAndCorrect.validateReps(0))
    }

    @Test
    fun validateWeight_20_returnsTrue() {
        assertTrue(ValidateAndCorrect.validateWeight(20.0))
    }

    @Test
    fun validateWeight_NaN_returnsFalse() {
        assertFalse(ValidateAndCorrect.validateWeight(Double.NaN))
    }

    @Test
    fun validateWeight_POSITIVE_INFINITY_returnsFalse() {
        assertFalse(ValidateAndCorrect.validateWeight(Double.POSITIVE_INFINITY))
    }
}
