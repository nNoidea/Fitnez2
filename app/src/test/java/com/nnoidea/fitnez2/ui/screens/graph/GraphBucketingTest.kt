package com.nnoidea.fitnez2.ui.screens.graph

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.nnoidea.fitnez2.ui.theme.AdaptiveGoldDark
import com.nnoidea.fitnez2.ui.theme.AdaptiveGoldLight
import com.nnoidea.fitnez2.ui.theme.AdaptiveGreenDark
import com.nnoidea.fitnez2.ui.theme.AdaptiveGreenLight
import com.nnoidea.fitnez2.ui.theme.AdaptiveRedDark
import com.nnoidea.fitnez2.ui.theme.AdaptiveRedLight
import com.nnoidea.fitnez2.ui.theme.adaptiveGold
import com.nnoidea.fitnez2.ui.theme.adaptiveGreen
import com.nnoidea.fitnez2.ui.theme.adaptiveRed
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphBucketingTest {

    

    @Test
    fun barCorners_formContinuousSilhouette() {
        val days = listOf(
            DaySession(date = 1000L, maxWeight = 10.0, volume = 100.0, totalSets = 1, totalReps = 1),
            DaySession(date = 2000L, maxWeight = 20.0, volume = 200.0, totalSets = 1, totalReps = 1),
            DaySession(date = 3000L, maxWeight = 30.0, volume = 300.0, totalSets = 1, totalReps = 1)
        )
        val bars = buildChartBars(days, SessionRange.SEVEN, CompareMetric.MAX_WEIGHT)
        assertEquals(2, bars.size)
        assertEquals(10.0, bars[0].leftValue, 0.0)
        assertEquals(20.0, bars[0].rightValue, 0.0)
        assertEquals(20.0, bars[1].leftValue, 0.0)
        assertEquals(30.0, bars[1].rightValue, 0.0)
        assertEquals(bars[0].rightValue, bars[1].leftValue, 0.0)
    }

    @Test
    fun barCorners_flatDataHasEqualCorners() {
        val days = listOf(
            DaySession(date = 1000L, maxWeight = 50.0, volume = 500.0, totalSets = 1, totalReps = 1),
            DaySession(date = 2000L, maxWeight = 50.0, volume = 500.0, totalSets = 1, totalReps = 1)
        )
        val bars = buildChartBars(days, SessionRange.SEVEN, CompareMetric.MAX_WEIGHT)
        assertEquals(1, bars.size)
        assertEquals(50.0, bars[0].leftValue, 0.0)
        assertEquals(50.0, bars[0].rightValue, 0.0)
    }

    @Test
    fun roundedPolygon_roundsEveryCorner() {
        val square = listOf(Offset(0f, 0f), Offset(100f, 0f), Offset(100f, 100f), Offset(0f, 100f))
        val poly = roundedPolygon(square, 10f)
        // Path starts at the end of corner 0, then one op per corner in order
        assertEquals(Offset(10f, 0f), poly.start)
        assertEquals(4, poly.ops.size)
        // Corner 0 trims 10px along both adjacent edges
        val last = poly.ops.last()
        assertEquals(Offset(0f, 10f), last.lineTo)
        assertEquals(Offset(0f, 0f), last.control)
        assertEquals(Offset(10f, 0f), last.end)
        // Path ends where it started: closed loop
        assertEquals(poly.start, last.end)
    }

    @Test
    fun roundedPolygon_clampsRadiusToShortestEdge() {
        val square = listOf(Offset(0f, 0f), Offset(100f, 0f), Offset(100f, 100f), Offset(0f, 100f))
        val poly = roundedPolygon(square, 60f)
        // Half of the 100px edge is the most any corner may take
        val last = poly.ops.last()
        assertEquals(Offset(0f, 50f), last.lineTo)
        assertEquals(Offset(50f, 0f), last.end)
    }

    @Test
    fun roundedPolygon_survivesDegeneratePoints() {
        val points = listOf(Offset(0f, 100f), Offset(0f, 50f), Offset(0f, 50f), Offset(60f, 20f))
        val poly = roundedPolygon(points, 6f)
        (listOf(poly.start) + poly.ops.flatMap { listOf(it.lineTo, it.control, it.end) })
            .forEach { assertTrue(it.x.isFinite() && it.y.isFinite()) }
    }

    private fun point(
        date: Long,
        weight: Double,
        sets: Int = 3,
        volume: Double = 0.0,
        isPr: Boolean = false,
        isLatest: Boolean = false
    ) = SessionPoint(
        date = date, maxWeight = weight, totalSets = sets, totalReps = sets * 10,
        volume = volume, isPr = isPr, isLatest = isLatest
    )

    @Test
    fun compareMetric_selectorsPickRightField() {
        val p = point(1L, weight = 80.0, volume = 240.0)
        assertEquals(80.0, CompareMetric.MAX_WEIGHT.select(p), 0.0)
        assertEquals(240.0, CompareMetric.VOLUME.select(p), 0.0)
        assertEquals(globalLocalization.labelGraphMaxWeight, CompareMetric.MAX_WEIGHT.label())
        assertEquals(globalLocalization.labelGraphVolume, CompareMetric.VOLUME.label())
    }

    @Test
    fun markPrFlags_spotlightsDistinctPeak() {
        val sessions = listOf(point(1L, 80.0), point(2L, 100.0), point(3L, 90.0))
        assertEquals(listOf(false, true, false), markPrFlags(sessions, CompareMetric.MAX_WEIGHT.select).map { it.isPr })
    }

    @Test
    fun markPrFlags_hidesFlatAndZeroSeries() {
        assertTrue(markPrFlags(emptyList(), CompareMetric.MAX_WEIGHT.select).none { it.isPr })
        assertTrue(markPrFlags(
            listOf(point(1L, 80.0), point(2L, 80.0)), CompareMetric.MAX_WEIGHT.select
        ).none { it.isPr })
        assertTrue(markPrFlags(
            listOf(point(1L, 0.0), point(2L, 0.0)), CompareMetric.MAX_WEIGHT.select
        ).none { it.isPr })
    }

    @Test
    fun markPrFlags_prefersMostRecentTie() {
        val sessions = listOf(point(1L, 100.0), point(2L, 100.0), point(3L, 80.0))
        assertEquals(listOf(false, true, false), markPrFlags(sessions, CompareMetric.MAX_WEIGHT.select).map { it.isPr })
    }

    @Test
    fun markPrFlags_supportsVolume() {
        val sessions = listOf(
            point(1L, weight = 100.0, volume = 500.0),
            point(2L, weight = 80.0, volume = 900.0)
        )
        assertEquals(listOf(false, true), markPrFlags(sessions, CompareMetric.VOLUME.select).map { it.isPr })
    }

    @Test
    fun heightLabels_keepEveryDistinctBar() {
        val values = listOf(98.0, 98.4, 98.8, 99.2, 99.4, 99.7, 100.0)
        assertEquals(values, heightLabelValues(values))
    }

    @Test
    fun heightLabels_collapseEqualValues() {
        assertEquals(listOf(10.0, 20.0, 30.0), heightLabelValues(listOf(10.0, 20.0, 20.0, 30.0)))
    }

    @Test
    fun heightLabels_handleEdgeCases() {
        assertEquals(emptyList<Double>(), heightLabelValues(emptyList()))
        assertEquals(listOf(7.0), heightLabelValues(listOf(7.0)))
        assertEquals(listOf(50.0), heightLabelValues(listOf(50.0, 50.0, 50.0)))
    }

    @Test
    fun heightLabels_preservesAllDistinctValuesWithoutDroppingClosePeak() {
        // User scenario: 1.03, 131.75, 133.33
        // All distinct values must be preserved so every label is shown
        val values = listOf(1.03, 1.03, 131.75, 133.33)
        val labels = heightLabelValues(values)
        assertEquals(listOf(1.03, 131.75, 133.33), labels)
    }

    @Test
    fun heightLabels_preservesDistinctCloseValues() {
        // Close values are preserved so each gets its own horizontal line and label
        assertEquals(listOf(0.0, 1.0, 2.0, 100.0), heightLabelValues(listOf(0.0, 1.0, 2.0, 100.0)))
    }

    @Test
    fun heightLabels_collapsesValuesSharingSameFormattedDisplayString() {
        // 8.4375 and 8.4417 both round to "8.44" -> should collapse to single gridline to avoid duplicate identical Y-axis labels
        val values = listOf(8.4375, 8.4417)
        val labels = heightLabelValues(values)
        assertEquals(listOf(8.4417), labels)
    }

    @Test
    fun resolveCanonicalValue_mapsValuesSharingFormattedDisplayToSameLabel() {
        val labelValues = listOf(8.4417, 10.0)
        assertEquals(8.4417, resolveCanonicalValue(8.4375, labelValues), 0.0001)
        assertEquals(8.4417, resolveCanonicalValue(8.4417, labelValues), 0.0001)
        assertEquals(10.0, resolveCanonicalValue(10.0, labelValues), 0.0001)
    }

    @Test
    fun computeNonOverlappingY_enforcesMinimumGapBetweenCloseValues() {
        val values = listOf(1.03, 131.75, 133.33)
        val minGap = 20f
        val yMap = computeNonOverlappingY(values, yTop = 28f, yBottom = 200f, minGapPx = minGap)
        val y133 = yMap[133.33]!!
        val y131 = yMap[131.75]!!
        val y1 = yMap[1.03]!!
        assertEquals(28f, y133, 0.01f)
        assertTrue("Gap between 133.33 and 131.75 must be >= $minGap, was ${y131 - y133}", (y131 - y133) >= minGap - 0.01f)
        assertTrue("Gap between 131.75 and 1.03 must be >= $minGap, was ${y1 - y131}", (y1 - y131) >= minGap - 0.01f)
    }

    @Test
    fun silhouetteCorners_listLeftEdgePlusEveryRightEdge() {
        // Corner values directly from bar segments without artificial neighbor averaging
        val bars = listOf(
            SessionPoint(date = 1L, maxWeight = 10.0, totalSets = 1, totalReps = 1, leftValue = 10.0, rightValue = 20.0),
            SessionPoint(date = 2L, maxWeight = 30.0, totalSets = 1, totalReps = 1, leftValue = 20.0, rightValue = 30.0)
        )
        assertEquals(
            listOf(10.0, 20.0, 30.0),
            silhouetteCornerValues(bars)
        )
    }

    @Test
    fun silhouetteCorners_handleEdgeCases() {
        assertEquals(emptyList<Double>(), silhouetteCornerValues(emptyList()))
        val single = listOf(SessionPoint(date = 1L, maxWeight = 7.0, totalSets = 1, totalReps = 1, leftValue = 7.0, rightValue = 7.0))
        assertEquals(listOf(7.0, 7.0), silhouetteCornerValues(single))
    }

    @Test
    fun groupRecordsByDay_groupsMultipleRecordsOnSameDay() {
        val zone = ZoneId.of("UTC")
        // Three records on day 1 (4 Oct 2026), one record on day 2 (5 Oct 2026)
        // 4 Oct 2026 10:00:00 UTC = 1791108000000L
        val t1 = 1791108000000L
        val t2 = t1 + 300000L // +5 min
        val t3 = t1 + 600000L // +10 min
        val t4 = t1 + 86400000L // next day

        val records = listOf(
            Record(exerciseId = "dips", sets = 3, reps = 10, weight = 20.0, date = t1),
            Record(exerciseId = "dips", sets = 3, reps = 10, weight = 20.0, date = t2),
            Record(exerciseId = "dips", sets = 1, reps = 1, weight = 999.0, date = t3),
            Record(exerciseId = "dips", sets = 2, reps = 8, weight = 25.0, date = t4)
        )

        val days = groupRecordsByDay(records, zone)
        assertEquals(2, days.size)

        // Day 1
        val day1 = days[0]
        assertEquals(999.0, day1.maxWeight, 0.0)
        // Volume = 3*10*20 + 3*10*20 + 1*1*999 = 600 + 600 + 999 = 2199.0
        assertEquals(2199.0, day1.volume, 0.0)
        assertEquals(7, day1.totalSets)
        assertEquals(61, day1.totalReps)

        // Day 2
        val day2 = days[1]
        assertEquals(25.0, day2.maxWeight, 0.0)
        assertEquals(2 * 8 * 25.0, day2.volume, 0.0)
    }

    @Test
    fun groupRecordsByDay_withRolloverHour_groupsLateNightIntoSameDay() {
        val zone = ZoneId.of("UTC")
        // Monday 23:30 (e.g. 2026-10-05 23:30:00 UTC)
        val mondayLate = java.time.LocalDate.of(2026, 10, 5).atTime(23, 30).atZone(zone).toInstant().toEpochMilli()
        // Tuesday 01:30 (2026-10-06 01:30:00 UTC)
        val tuesdayEarly = java.time.LocalDate.of(2026, 10, 6).atTime(1, 30).atZone(zone).toInstant().toEpochMilli()

        val records = listOf(
            Record(exerciseId = "bench", sets = 3, reps = 10, weight = 100.0, date = mondayLate),
            Record(exerciseId = "bench", sets = 2, reps = 8, weight = 110.0, date = tuesdayEarly)
        )

        // With rolloverHour = 0 (standard midnight), they split into 2 sessions
        val splitDays = groupRecordsByDay(records, zone, rolloverHour = 0)
        assertEquals(2, splitDays.size)

        // With rolloverHour = 4, both belong to the Monday session
        val joinedDays = groupRecordsByDay(records, zone, rolloverHour = 4)
        assertEquals(1, joinedDays.size)
        assertEquals(110.0, joinedDays[0].maxWeight, 0.0)
        assertEquals(5, joinedDays[0].totalSets)
        assertEquals(30 + 16, joinedDays[0].totalReps)
    }

    @Test
    fun buildChartBars_eightDaysWithPeakOnLastDay_noArtificial509Point5() {
        // 7 days of 20kg, 8th day 999kg -> exactly 7 bars
        val days = (0 until 7).map { i ->
            DaySession(date = 1000L + i * 86400000L, maxWeight = 20.0, volume = 200.0, totalSets = 3, totalReps = 30)
        } + DaySession(date = 1000L + 7 * 86400000L, maxWeight = 999.0, volume = 999.0, totalSets = 1, totalReps = 1)

        val bars = buildChartBars(days, SessionRange.SEVEN, CompareMetric.MAX_WEIGHT)
        assertEquals(7, bars.size)

        // First 6 bars are flat at 20.0
        for (i in 0 until 6) {
            assertEquals("Bar $i leftValue", 20.0, bars[i].leftValue, 0.0)
            assertEquals("Bar $i rightValue", 20.0, bars[i].rightValue, 0.0)
        }

        // 7th (latest) bar connects 20.0 to 999.0!
        assertEquals(20.0, bars[6].leftValue, 0.0)
        assertEquals(999.0, bars[6].rightValue, 0.0)
        assertTrue(bars[6].isLatest)

        // Shared corners between all adjacent bars
        for (i in 0 until bars.lastIndex) {
            assertEquals("Bar $i right must equal Bar ${i + 1} left", bars[i].rightValue, bars[i + 1].leftValue, 0.0)
        }

        // Silhouette corners: ONLY 20.0 and 999.0, NO 509.5!
        val corners = silhouetteCornerValues(bars)
        val distinctLabels = heightLabelValues(corners)
        assertEquals(listOf(20.0, 999.0), distinctLabels)
    }

    @Test
    fun buildChartBars_lowDataShowsFewerBars() {
        val d1 = DaySession(date = 1000L, maxWeight = 50.0, volume = 500.0, totalSets = 3, totalReps = 30)
        val d2 = DaySession(date = 2000L, maxWeight = 60.0, volume = 600.0, totalSets = 3, totalReps = 30)
        val d3 = DaySession(date = 3000L, maxWeight = 70.0, volume = 700.0, totalSets = 3, totalReps = 30)

        // 0 days
        assertEquals(emptyList<SessionPoint>(), buildChartBars(emptyList(), SessionRange.SEVEN, CompareMetric.MAX_WEIGHT))

        // 1 day -> 1 flat bar
        val singleBar = buildChartBars(listOf(d1), SessionRange.SEVEN, CompareMetric.MAX_WEIGHT)
        assertEquals(1, singleBar.size)
        assertEquals(50.0, singleBar[0].leftValue, 0.0)
        assertEquals(50.0, singleBar[0].rightValue, 0.0)
        assertTrue(singleBar[0].isLatest)

        // 3 days -> 2 transition bars
        val twoBars = buildChartBars(listOf(d1, d2, d3), SessionRange.SEVEN, CompareMetric.MAX_WEIGHT)
        assertEquals(2, twoBars.size)
        assertEquals(50.0, twoBars[0].leftValue, 0.0)
        assertEquals(60.0, twoBars[0].rightValue, 0.0)
        assertEquals(60.0, twoBars[1].leftValue, 0.0)
        assertEquals(70.0, twoBars[1].rightValue, 0.0)
        assertTrue(twoBars[1].isLatest)
    }

    @Test
    fun buildChartBars_thirtyDays_collapsesToSevenBarsWithSharedCorners() {
        val days = (0 until 30).map { i ->
            DaySession(
                date = 1000L + i * 86400000L,
                maxWeight = 20.0 + i,
                volume = (20.0 + i) * 10,
                totalSets = 3,
                totalReps = 30
            )
        }

        val bars = buildChartBars(days, SessionRange.THIRTY, CompareMetric.MAX_WEIGHT)
        assertEquals(7, bars.size)

        // Last bar is latest day itself
        assertEquals(days.last().maxWeight, bars.last().rightValue, 0.0)
        assertTrue(bars.last().isLatest)

        // Shared corners across all bars
        for (i in 0 until bars.lastIndex) {
            assertEquals("Bar $i right must equal Bar ${i + 1} left", bars[i].rightValue, bars[i + 1].leftValue, 0.0)
        }
    }

    @Test
    fun buildChartBars_twoRange_takesLastTwoDaysToComparePreviousWithCurrent() {
        val days = listOf(
            DaySession(date = 1000L, maxWeight = 40.0, volume = 400.0, totalSets = 3, totalReps = 30),
            DaySession(date = 2000L, maxWeight = 50.0, volume = 500.0, totalSets = 3, totalReps = 30),
            DaySession(date = 3000L, maxWeight = 60.0, volume = 600.0, totalSets = 3, totalReps = 30)
        )
        val bars = buildChartBars(days, SessionRange.TWO, CompareMetric.MAX_WEIGHT)
        // Must take last 2 days (day 2000L and day 3000L) -> produces 1 transition bar from 50.0 to 60.0
        assertEquals(1, bars.size)
        assertEquals(50.0, bars[0].leftValue, 0.0)
        assertEquals(60.0, bars[0].rightValue, 0.0)
        assertEquals(2000L, bars[0].startDate)
        assertEquals(3000L, bars[0].date)
        assertTrue(bars[0].isLatest)
    }

    @Test
    fun resolveDefaultExerciseId_emptyExercises_returnsNull() {
        assertNull(resolveDefaultExerciseId(exercises = emptyList(), latestRecordExerciseId = null))
        assertNull(resolveDefaultExerciseId(exercises = emptyList(), latestRecordExerciseId = "ex2"))
    }

    @Test
    fun resolveDefaultExerciseId_hasLatestRecord_returnsLatestRecordExercise() {
        val exercises = listOf(
            Exercise(id = "ex1", name = "Squat"),
            Exercise(id = "ex2", name = "Bench")
        )
        val selected = resolveDefaultExerciseId(
            exercises = exercises,
            latestRecordExerciseId = "ex2"
        )
        assertEquals("ex2", selected)
    }

    @Test
    fun resolveDefaultExerciseId_latestRecordExerciseNotInExercises_fallsBackToFirstExercise() {
        val exercises = listOf(
            Exercise(id = "ex1", name = "Squat"),
            Exercise(id = "ex2", name = "Bench")
        )
        val selected = resolveDefaultExerciseId(
            exercises = exercises,
            latestRecordExerciseId = "deleted_or_unknown"
        )
        assertEquals("ex1", selected)
    }

    @Test
    fun resolveDefaultExerciseId_noLatestRecord_fallsBackToFirstExercise() {
        val exercises = listOf(
            Exercise(id = "ex1", name = "Squat"),
            Exercise(id = "ex2", name = "Bench")
        )
        val selected = resolveDefaultExerciseId(
            exercises = exercises,
            latestRecordExerciseId = null
        )
        assertEquals("ex1", selected)
    }

    @Test
    fun formatMaxTwoDecimals_formatsIntegersAndDecimalsCorrectly() {
        assertEquals("20", formatMaxTwoDecimals(20.0, java.util.Locale.US))
        assertEquals("20.5", formatMaxTwoDecimals(20.5, java.util.Locale.US))
        assertEquals("20.25", formatMaxTwoDecimals(20.25, java.util.Locale.US))
        assertEquals("20.25", formatMaxTwoDecimals(20.254, java.util.Locale.US))
        assertEquals("20.26", formatMaxTwoDecimals(20.256, java.util.Locale.US))
        assertEquals("0", formatMaxTwoDecimals(0.0, java.util.Locale.US))
        assertEquals("0", formatMaxTwoDecimals(0.001, java.util.Locale.US))
        assertEquals("0", formatMaxTwoDecimals(-0.001, java.util.Locale.US))
        assertEquals("-15.5", formatMaxTwoDecimals(-15.5, java.util.Locale.US))
        assertEquals("-15.25", formatMaxTwoDecimals(-15.25, java.util.Locale.US))
        assertEquals("--", formatMaxTwoDecimals(Double.NaN, java.util.Locale.US))
        assertEquals("--", formatMaxTwoDecimals(Double.POSITIVE_INFINITY, java.util.Locale.US))
    }

    @Test
    fun adaptiveColors_adaptToBackgroundLuminance() {
        // Dark background: selects dark variants
        assertEquals(AdaptiveRedDark, adaptiveRed(Color(0xFF1E1B16)))
        assertEquals(AdaptiveGoldDark, adaptiveGold(Color(0xFF1E1B16)))
        assertEquals(AdaptiveGreenDark, adaptiveGreen(Color(0xFF1E1B16)))

        // Light background: selects light variants
        assertEquals(AdaptiveRedLight, adaptiveRed(Color(0xFFFBF8F2)))
        assertEquals(AdaptiveGoldLight, adaptiveGold(Color(0xFFFBF8F2)))
        assertEquals(AdaptiveGreenLight, adaptiveGreen(Color(0xFFFBF8F2)))

        // Mid-tone Material You gray (#A0A0A0): must select high-contrast light variants
        assertEquals(AdaptiveRedLight, adaptiveRed(Color(0xFFA0A0A0)))
        assertEquals(AdaptiveGreenLight, adaptiveGreen(Color(0xFFA0A0A0)))
    }

    @Test
    fun calculateArrowDirection_returnsCorrectSlope() {
        assertEquals(ArrowDirection.UP, calculateArrowDirection(5.0))
        assertEquals(ArrowDirection.DOWN, calculateArrowDirection(-2.5))
        assertEquals(ArrowDirection.NEUTRAL, calculateArrowDirection(0.0))
        assertEquals(ArrowDirection.NEUTRAL, calculateArrowDirection(null))
        // Values that round away to 0 (< 0.005) must evaluate to NEUTRAL
        assertEquals(ArrowDirection.NEUTRAL, calculateArrowDirection(0.0033))
        assertEquals(ArrowDirection.NEUTRAL, calculateArrowDirection(-0.0033))
        assertEquals(ArrowDirection.UP, calculateArrowDirection(0.01))
        assertEquals(ArrowDirection.DOWN, calculateArrowDirection(-0.01))
    }

    @Test
    fun roundToTwoDecimals_roundsHalfUpCorrectly() {
        assertEquals(0.61, roundToTwoDecimals(0.6067), 0.0001)
        assertEquals(0.61, roundToTwoDecimals(0.6100), 0.0001)
        assertEquals(8.75, roundToTwoDecimals(8.7500), 0.0001)
        assertEquals(8.79, roundToTwoDecimals(8.7917), 0.0001)
    }

    @Test
    fun groupRecordsByDay_roundsSessionsToTwoDecimals_producesIdenticalOneRmForCloseSubGramDeltas() {
        val zone = ZoneId.of("UTC")
        val day1Records = listOf(
            Record(exerciseId = "rev_fly", sets = 1, reps = 5, weight = 0.5, date = 1000L),
            Record(exerciseId = "rev_fly", sets = 1, reps = 5, weight = 0.5, date = 1001L),
            Record(exerciseId = "rev_fly", sets = 1, reps = 2, weight = 0.5, date = 1002L)
        )
        val day2Records = listOf(
            Record(exerciseId = "rev_fly", sets = 1, reps = 5, weight = 0.5, date = 2000L),
            Record(exerciseId = "rev_fly", sets = 1, reps = 5, weight = 0.5, date = 2001L),
            Record(exerciseId = "rev_fly", sets = 1, reps = 3, weight = 0.5, date = 2002L)
        )
        val day1 = groupRecordsByDay(day1Records, zone).first()
        val day2 = groupRecordsByDay(day2Records, zone).first()

        assertEquals(0.61, day1.oneRm, 0.0001)
        assertEquals(0.61, day2.oneRm, 0.0001)
    }

    @Test
    fun calculateDayOneRm_weightedPeakAlgorithm_computesExpectedScore() {
        // User example:
        // Row 1: Sets: 2, Reps: 8, Weight: 50 -> Base = 50 * (1 + 8/30) = 63.333..., repWork = 50 * (8/30) = 13.333...
        // Row 2: Sets: 1, Reps: 5, Weight: 50 -> Base = 50 * (1 + 5/30) = 58.333..., repWork = 50 * (5/30) = 8.333...
        // Peak = 63.333...
        // Total repWork = 2 * 13.333... + 1 * 8.333... = 35.0
        // Extra repWork beyond peak set = 35.0 - 13.333... = 21.666...
        // Volume Bonus = 0.20 * 21.666... = 4.3333...
        // Total = 63.333... + 4.3333... = 67.6667...
        val records = listOf(
            Record(exerciseId = "bench", sets = 2, reps = 8, weight = 50.0, date = 1000L),
            Record(exerciseId = "bench", sets = 1, reps = 5, weight = 50.0, date = 1001L)
        )
        val score = calculateDayOneRm(records)
        assertEquals(67.6667, score, 0.001)
    }

    @Test
    fun calculateDayOneRm_handlesEmptyAndZeroWeight() {
        assertEquals(0.0, calculateDayOneRm(emptyList()), 0.0)
        val zeroWeight = listOf(
            Record(exerciseId = "pullup", sets = 3, reps = 10, weight = 0.0, date = 1000L)
        )
        assertEquals(0.0, calculateDayOneRm(zeroWeight), 0.0)
    }

    @Test
    fun calculateDayOneRm_singleSetAlwaysEqualsExactEpleyDirectly() {
        // Any exercise with 1 set, X reps, X weight must equal Epley directly: weight * (1 + reps / 30)
        val testCases = listOf(
            Triple(10.0, 2, 10.0 * (1.0 + 2.0 / 30.0)),
            Triple(100.0, 5, 100.0 * (1.0 + 5.0 / 30.0)),
            Triple(22.5, 12, 22.5 * (1.0 + 12.0 / 30.0)),
            Triple(6.25, 9, 6.25 * (1.0 + 9.0 / 30.0))
        )
        for ((weight, reps, expectedEpley) in testCases) {
            val singleSet = listOf(
                Record(exerciseId = "ex", sets = 1, reps = reps, weight = weight, date = 1000L)
            )
            assertEquals(expectedEpley, calculateDayOneRm(singleSet), 0.0001)
        }
    }

    @Test
    fun calculateDayOneRm_oneSetTwoRepsIsGreaterThanTwoSetsOneRep() {
        // 1x2x1kg needs to be bigger than 2x1x1kg (reps are harder than sets)
        val oneSetTwoReps = listOf(
            Record(exerciseId = "ex", sets = 1, reps = 2, weight = 1.0, date = 1000L)
        )
        val twoSetsOneRep = listOf(
            Record(exerciseId = "ex", sets = 2, reps = 1, weight = 1.0, date = 1000L)
        )
        val scoreOneSetTwoReps = calculateDayOneRm(oneSetTwoReps)
        val scoreTwoSetsOneRep = calculateDayOneRm(twoSetsOneRep)
        assertTrue(
            "1x2x1kg ($scoreOneSetTwoReps) must be greater than 2x1x1kg ($scoreTwoSetsOneRep)",
            scoreOneSetTwoReps > scoreTwoSetsOneRep
        )
    }

    @Test
    fun calculateDayOneRm_nineNineSixVsNineNineSevenAtSixPointTwentyFive_showsSignificantIncreaseInLastTwoDigits() {
        // 9x9x6 vs 9x9x7 at 6.25kg needs to show significant increase at at least last 2 digits
        val session996 = listOf(
            Record(exerciseId = "cable_raise", sets = 2, reps = 9, weight = 6.25, date = 1000L),
            Record(exerciseId = "cable_raise", sets = 1, reps = 6, weight = 6.25, date = 1001L)
        )
        val session997 = listOf(
            Record(exerciseId = "cable_raise", sets = 2, reps = 9, weight = 6.25, date = 2000L),
            Record(exerciseId = "cable_raise", sets = 1, reps = 7, weight = 6.25, date = 2001L)
        )

        val score996 = calculateDayOneRm(session996)
        val score997 = calculateDayOneRm(session997)

        val formatted996 = formatMaxTwoDecimals(score996, java.util.Locale.US)
        val formatted997 = formatMaxTwoDecimals(score997, java.util.Locale.US)

        assertTrue(
            "Expected score difference to be at least 0.02, but was ${score997 - score996}",
            score997 - score996 >= 0.02
        )
        assertNotEquals(
            "Formatted strings must be distinct, but both were '$formatted996'",
            formatted996,
            formatted997
        )
    }

    @Test
    fun compareMetric_supportsOneRm() {
        val p = SessionPoint(
            date = 1L,
            maxWeight = 50.0,
            totalSets = 3,
            totalReps = 21,
            volume = 1050.0,
            oneRm = 81.83
        )
        val d = DaySession(
            date = 1L,
            maxWeight = 50.0,
            volume = 1050.0,
            totalSets = 3,
            totalReps = 21,
            oneRm = 81.83
        )
        assertEquals(81.83, CompareMetric.ONE_RM.select(p), 0.001)
        assertEquals(81.83, CompareMetric.ONE_RM.select(d), 0.001)
        assertEquals("1RM", CompareMetric.ONE_RM.label())
    }

    @Test
    fun groupRecordsByDay_calculatesOneRmScore() {
        val zone = ZoneId.of("UTC")
        val records = listOf(
            Record(exerciseId = "bench", sets = 2, reps = 8, weight = 50.0, date = 1000L),
            Record(exerciseId = "bench", sets = 1, reps = 5, weight = 50.0, date = 1001L)
        )
        val days = groupRecordsByDay(records, zone)
        assertEquals(1, days.size)
        assertEquals(67.67, days[0].oneRm, 0.001)
    }

    @Test
    fun buildChartBars_supportsOneRmMetric() {
        val d1 = DaySession(date = 1000L, maxWeight = 50.0, volume = 500.0, totalSets = 2, totalReps = 16, oneRm = 70.0)
        val d2 = DaySession(date = 2000L, maxWeight = 60.0, volume = 600.0, totalSets = 2, totalReps = 16, oneRm = 85.0)

        val bars = buildChartBars(listOf(d1, d2), SessionRange.SEVEN, CompareMetric.ONE_RM)
        assertEquals(1, bars.size)
        assertEquals(70.0, bars[0].leftValue, 0.001)
        assertEquals(85.0, bars[0].rightValue, 0.001)
        assertEquals(85.0, bars[0].oneRm, 0.001)
    }

    @Test
    fun calculateDayOneRm_capsVolumeBonusToPreventJunkVolumeExploit() {
        // 1 set of 100 reps @ 10kg collapses strictly to Epley: 10 * (1 + 100/30) = 43.333
        val repUser = listOf(
            Record(exerciseId = "dips", sets = 1, reps = 100, weight = 10.0, date = 1000L)
        )
        // 100 sets of 1 rep @ 10kg
        val setSpammer = listOf(
            Record(exerciseId = "dips", sets = 100, reps = 1, weight = 10.0, date = 2000L)
        )

        val repScore = calculateDayOneRm(repUser)
        val setScore = calculateDayOneRm(setSpammer)

        // 100 reps must beat 100 sets of 1 rep
        assertTrue("100 reps ($repScore) must be higher than 100 sets ($setScore)", repScore > setScore)
        assertEquals(43.333, repScore, 0.01)
        // Set spammer is capped at Peak (10.333) + 30% Peak (3.10) = 13.433
        assertEquals(13.433, setScore, 0.01)
    }

    @Test
    fun calculateDayOneRm_rewardsExtraRepOnWorkingSets() {
        val day1 = listOf(
            Record(exerciseId = "bench", sets = 2, reps = 8, weight = 50.0, date = 1000L),
            Record(exerciseId = "bench", sets = 1, reps = 5, weight = 50.0, date = 1001L)
        )
        val day2 = listOf(
            Record(exerciseId = "bench", sets = 2, reps = 8, weight = 50.0, date = 2000L),
            Record(exerciseId = "bench", sets = 1, reps = 6, weight = 50.0, date = 2001L)
        )
        val score1 = calculateDayOneRm(day1)
        val score2 = calculateDayOneRm(day2)
        assertEquals(67.667, score1, 0.01)
        assertEquals(68.000, score2, 0.01)
        assertTrue(score2 > score1)
    }

    @Test
    fun calculateDayOneRm_repsBeatSetsForEqualTonnage() {
        // 1 set of 2 reps @ 1kg (2 total reps, 2kg tonnage) -> exact Epley: 1 * (1 + 2/30) = 1.0667
        val twoReps = listOf(
            Record(exerciseId = "row", sets = 1, reps = 2, weight = 1.0, date = 1000L)
        )
        // 2 sets of 1 rep @ 1kg (2 total reps, 2kg tonnage) -> 1.040
        val twoSets = listOf(
            Record(exerciseId = "row", sets = 2, reps = 1, weight = 1.0, date = 2000L)
        )
        val scoreReps = calculateDayOneRm(twoReps)
        val scoreSets = calculateDayOneRm(twoSets)
        assertTrue("1 set of 2 reps ($scoreReps) must beat 2 sets of 1 rep ($scoreSets)", scoreReps > scoreSets)
        assertEquals(1.0667, scoreReps, 0.001)
        assertEquals(1.040, scoreSets, 0.001)
    }

    private fun cornerBar(index: Int, left: Double, right: Double) = SessionPoint(
        date = index.toLong(),
        maxWeight = right,
        totalSets = 1,
        totalReps = 1,
        leftValue = left,
        rightValue = right
    )

    @Test
    fun cornerLabelPositions_coversFirstLeftAndEveryRight() {
        val bars = listOf(
            cornerBar(0, left = 10.0, right = 20.0),
            cornerBar(1, left = 20.0, right = 30.0)
        )
        // paddingLeft 8, slotWidth 100, gap 8
        val points = cornerLabelPositions(bars, 8f, 100f, 8f)
        assertEquals(3, points.size)
        assertEquals(10.0, points[0].value, 0.0)
        assertEquals(8f + 4f, points[0].x, 0.001f)
        assertEquals(20.0, points[1].value, 0.0)
        assertEquals(8f + 100f, points[1].x, 0.001f)
        assertEquals(30.0, points[2].value, 0.0)
        assertEquals(8f + 200f - 4f, points[2].x, 0.001f)
    }

    @Test
    fun cornerLabelPositions_emptyBarsGiveNoPoints() {
        assertTrue(cornerLabelPositions(emptyList(), 8f, 100f, 8f).isEmpty())
    }

    private fun labelRun(vararg values: Double) =
        values.toList().mapIndexed { i, v -> CornerLabelPoint(v, i.toFloat()) }

    @Test
    fun collapseConsecutiveLabels_runCollapsesToLatestBar() {
        val collapsed = collapseConsecutiveLabels(labelRun(1.0, 1.0, 1.0, 3.0, 3.0, 3.0))
        assertEquals(listOf(CornerLabelPoint(1.0, 2f), CornerLabelPoint(3.0, 5f)), collapsed)
    }

    @Test
    fun collapseConsecutiveLabels_nonConsecutiveRepeatsAreKept() {
        val collapsed = collapseConsecutiveLabels(labelRun(1.0, 1.0, 2.0, 4.0, 1.0, 3.0, 3.0, 3.0))
        assertEquals(
            listOf(
                CornerLabelPoint(1.0, 1f),
                CornerLabelPoint(2.0, 2f),
                CornerLabelPoint(4.0, 3f),
                CornerLabelPoint(1.0, 4f),
                CornerLabelPoint(3.0, 7f)
            ),
            collapsed
        )
    }

    @Test
    fun collapseConsecutiveLabels_shortInputsPassThrough() {
        assertEquals(emptyList<CornerLabelPoint>(), collapseConsecutiveLabels(emptyList()))
        val single = listOf(CornerLabelPoint(1.0, 0f))
        assertEquals(single, collapseConsecutiveLabels(single))
        assertEquals(
            listOf(CornerLabelPoint(2.0, 2f)),
            collapseConsecutiveLabels(labelRun(2.0, 2.0, 2.0))
        )
    }

    @Test
    fun centeredLabelPillRect_floatsAboveLine() {
        val pill = centeredLabelPillRect(
            textWidth = 40f,
            ascent = -10f,
            descent = 3f,
            centerX = 100f,
            lineY = 80f,
            liftGap = 3f,
            paddingH = 5f,
            paddingV = 2f,
            minLeft = 0f,
            maxRight = 200f
        )
        // Bottom hovers liftGap above the line, horizontally centered.
        assertEquals(80f - 3f, pill.bottom, 0.001f)
        assertEquals(100f - 25f, pill.left, 0.001f)
        assertEquals(100f + 25f, pill.right, 0.001f)
        assertEquals((3f + 10f + 2 * 2f), pill.height, 0.001f)
    }

    @Test
    fun centeredLabelPillRect_clampsInsideBounds() {
        val leftPill = centeredLabelPillRect(
            textWidth = 40f, ascent = -10f, descent = 3f, centerX = 10f, lineY = 80f, liftGap = 3f,
            paddingH = 5f, paddingV = 2f, minLeft = 8f, maxRight = 200f
        )
        assertEquals(8f, leftPill.left, 0.001f)
        assertEquals(80f - 3f, leftPill.bottom, 0.001f)
        val rightPill = centeredLabelPillRect(
            textWidth = 40f, ascent = -10f, descent = 3f, centerX = 195f, lineY = 80f, liftGap = 3f,
            paddingH = 5f, paddingV = 2f, minLeft = 8f, maxRight = 200f
        )
        assertEquals(200f, rightPill.right, 0.001f)
        assertEquals(80f - 3f, rightPill.bottom, 0.001f)
    }
}



