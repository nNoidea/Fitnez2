package com.nnoidea.fitnez2.ui.screens.graph

import com.nnoidea.fitnez2.data.entities.Record

/**
 * Weighted Peak Algorithm (Epley Score per session/day):
 * - Base_i = Weight_i * (1 + Reps_i / 30.0)
 * - Peak = max(Base_i)
 * - RepWork_i = Weight_i * (Reps_i / 30.0)
 * - ExtraRepWork = max(0, sum(Sets_i * RepWork_i) - PeakRepWork)
 * - Volume Bonus = min(VOLUME_BONUS_RATIO * ExtraRepWork, Peak * 0.30)
 * - Session Score = Peak + Volume Bonus
 *
 * Guarantees:
 * 1. Single-set exercises strictly collapse to exact Epley.
 * 2. Reps beat sets: 1 set of 2 reps > 2 sets of 1 rep for equal tonnage.
 * 3. Rep progressions on working sets (e.g. 9,9,6 vs 9,9,7 at 6.25kg) show clear, distinguishable strength gains.
 */
object OneRmConfig {
    const val EPLEY_REP_DIVISOR = 30.0
    const val VOLUME_BONUS_RATIO = 0.20
    const val MAX_VOLUME_BONUS_FRACTION_OF_PEAK = 0.30
}

fun calculateDayOneRm(records: List<Record>): Double {
    if (records.isEmpty()) return 0.0
    val bases = records.map {
        if (it.weight <= 0.0 || it.reps <= 0) 0.0
        else it.weight * (1.0 + it.reps / OneRmConfig.EPLEY_REP_DIVISOR)
    }
    val peak = bases.maxOrNull() ?: 0.0
    if (peak <= 0.0) return 0.0

    // Rep-work component for each record: weight * (reps / 30.0)
    // The peak set is the primary lift. Extra volume beyond the peak set is the rep-work performed in supplementary sets.
    val repWorks = records.map {
        if (it.weight <= 0.0 || it.reps <= 0) 0.0
        else it.weight * (it.reps / OneRmConfig.EPLEY_REP_DIVISOR)
    }
    val peakIndex = bases.indexOf(peak)
    val peakRepWork = if (peakIndex >= 0) repWorks[peakIndex] else 0.0

    val totalRepWork = records.indices.sumOf { i -> records[i].sets * repWorks[i] }
    val extraRepWork = maxOf(0.0, totalRepWork - peakRepWork)

    val rawBonus = OneRmConfig.VOLUME_BONUS_RATIO * extraRepWork
    val cappedBonus = minOf(rawBonus, peak * OneRmConfig.MAX_VOLUME_BONUS_FRACTION_OF_PEAK)
    return peak + cappedBonus
}
