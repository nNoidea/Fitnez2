package com.nnoidea.fitnez2.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateAndCorrectTest {

    @Test
    fun sets_validPositiveInteger_returnsInt() {
        assertEquals(1, ValidateAndCorrect.sets("1"))
        assertEquals(5, ValidateAndCorrect.sets("5"))
        assertEquals(100, ValidateAndCorrect.sets("100"))
    }

    @Test
    fun sets_leadingZeros_parsesCorrectly() {
        assertEquals(1, ValidateAndCorrect.sets("01"))
        assertEquals(7, ValidateAndCorrect.sets("007"))
    }

    @Test
    fun sets_integerWithDecimalPointZero_parsesCorrectly() {
        assertEquals(5, ValidateAndCorrect.sets("5.0"))
        assertEquals(10, ValidateAndCorrect.sets("10.00"))
    }

    @Test
    fun sets_fractionalDecimal_returnsNull() {
        assertNull(ValidateAndCorrect.sets("5.5"))
        assertNull(ValidateAndCorrect.sets("0.1"))
    }

    @Test
    fun sets_zeroOrNegative_returnsNull() {
        assertNull(ValidateAndCorrect.sets("0"))
        assertNull(ValidateAndCorrect.sets("-1"))
        assertNull(ValidateAndCorrect.sets("-5"))
    }

    @Test
    fun sets_blankOrNonNumeric_returnsNull() {
        assertNull(ValidateAndCorrect.sets(""))
        assertNull(ValidateAndCorrect.sets("   "))
        assertNull(ValidateAndCorrect.sets("abc"))
        assertNull(ValidateAndCorrect.sets("12a"))
    }

    @Test
    fun reps_validPositiveInteger_returnsInt() {
        assertEquals(1, ValidateAndCorrect.reps("1"))
        assertEquals(12, ValidateAndCorrect.reps("12"))
        assertEquals(10, ValidateAndCorrect.reps("10.0"))
    }

    @Test
    fun reps_invalidValues_returnsNull() {
        assertNull(ValidateAndCorrect.reps("0"))
        assertNull(ValidateAndCorrect.reps("-3"))
        assertNull(ValidateAndCorrect.reps("8.5"))
        assertNull(ValidateAndCorrect.reps(""))
    }

    @Test
    fun weight_validNumbers_returnsDouble() {
        assertEquals(100.0, ValidateAndCorrect.weight("100")!!, 0.001)
        assertEquals(72.5, ValidateAndCorrect.weight("72.5")!!, 0.001)
        assertEquals(0.0, ValidateAndCorrect.weight("0")!!, 0.001)
        assertEquals(-5.0, ValidateAndCorrect.weight("-5")!!, 0.001)
        assertEquals(0.25, ValidateAndCorrect.weight(".25")!!, 0.001)
    }

    @Test
    fun weight_invalidInputs_returnsNull() {
        assertNull(ValidateAndCorrect.weight(""))
        assertNull(ValidateAndCorrect.weight("   "))
        assertNull(ValidateAndCorrect.weight("abc"))
        assertNull(ValidateAndCorrect.weight("NaN"))
        assertNull(ValidateAndCorrect.weight("Infinity"))
        assertNull(ValidateAndCorrect.weight("-Infinity"))
    }
}
