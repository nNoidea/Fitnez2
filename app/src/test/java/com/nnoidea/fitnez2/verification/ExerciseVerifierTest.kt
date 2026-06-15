package com.nnoidea.fitnez2.verification

import com.nnoidea.fitnez2.data.entities.Exercise
import kotlinx.coroutines.runBlocking
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

    @Test
    fun validateIdNotSet_zero_passes() {
        ExerciseVerifier.validateIdNotSet("")
    }

    @Test
    fun validateIdNotSet_positive_throws() {
        try {
            ExerciseVerifier.validateIdNotSet("5")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateIdNotSet_negative_throws() {
        try {
            ExerciseVerifier.validateIdNotSet("-1")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateNotDuplicate_notFound_passes() = runBlocking {
        ExerciseVerifier.validateNotDuplicate("Deadlift") { null }
    }

    @Test
    fun validateNotDuplicate_found_throws() = runBlocking {
        val existingExercise = Exercise( id = "1", name = "Deadlift")
        try {
            ExerciseVerifier.validateNotDuplicate("Deadlift") { existingExercise }
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }

    @Test
    fun validateUpdateAllowed_sameIdRename_passes() = runBlocking {
        val existingExercise = Exercise( id = "1", name = "Bench Press")
        ExerciseVerifier.validateUpdateAllowed("1", "Bench Press") { existingExercise }
    }

    @Test
    fun validateUpdateAllowed_newNameNotFound_passes() = runBlocking {
        ExerciseVerifier.validateUpdateAllowed("1", "New Name") { null }
    }

    @Test
    fun validateUpdateAllowed_differentIdConflict_throws() = runBlocking {
        val conflictingExercise = Exercise( id = "2", name = "Squat")
        try {
            ExerciseVerifier.validateUpdateAllowed("1", "Squat") { conflictingExercise }
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.isNotEmpty())
        }
    }
}
