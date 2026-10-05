package com.nnoidea.fitnez2.verification

import org.junit.Assert.*
import org.junit.Test

class ExerciseVerifierTest {

    @Test
    fun validateName_validNormalName_returnsTrimmed() {
        val result = ExerciseVerifier.validateName("Bench Press")
        assertEquals("Bench Press", result)
    }

    @Test
    fun validateName_whitespace_trimmed() {
        val result = ExerciseVerifier.validateName("  Squat  ")
        assertEquals("Squat", result)
    }

    @Test
    fun validateName_blankString_throws() {
        try {
            ExerciseVerifier.validateName("")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateName_whitespaceOnly_throws() {
        try {
            ExerciseVerifier.validateName("   ")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }
}
