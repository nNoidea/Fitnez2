package com.nnoidea.fitnez2.verification

import com.nnoidea.fitnez2.core.localization.globalLocalization

object RecordVerifier {

    fun validateSets(sets: Int) {
        if (sets <= 0) {
            throw IllegalArgumentException(globalLocalization.errorSetsPositive)
        }
    }

    fun validateReps(reps: Int) {
        if (reps <= 0) {
            throw IllegalArgumentException(globalLocalization.errorRepsPositive)
        }
    }

    fun validateWeight(weight: Double) {
        if (weight.isNaN() || weight.isInfinite()) {
            throw IllegalArgumentException(globalLocalization.errorWeightInvalid)
        }
    }

    fun validateRecord(sets: Int, reps: Int, weight: Double) {
        validateSets(sets)
        validateReps(reps)
        validateWeight(weight)
    }
}
