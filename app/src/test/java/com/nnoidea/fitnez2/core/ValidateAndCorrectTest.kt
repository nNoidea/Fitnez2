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
}
