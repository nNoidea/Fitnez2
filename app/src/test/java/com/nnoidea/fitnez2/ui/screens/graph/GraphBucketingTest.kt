package com.nnoidea.fitnez2.ui.screens.graph

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphBucketingTest {

    private fun sessions(n: Int, prIndex: Int = -1): List<SessionPoint> {
        return (0 until n).map { i ->
            SessionPoint(
                date = 1000L + i,
                maxWeight = 20.0 + i,
                totalSets = 3,
                totalReps = 30,
                isPr = i == prIndex,
                isLatest = i == n - 1
            )
        }
    }

    @Test
    fun seven_showsIndividualSessions() {
        val input = sessions(7)
        val out = bucketSessions(input)
        assertEquals(7, out.size)
        assertEquals(input, out)
    }

    @Test
    fun thirty_collapsesToExactlySevenBars() {
        val input = sessions(30)
        val out = bucketSessions(input)
        assertEquals(7, out.size)
        // Even split of first 29 into 6 -> 5,5,5,5,5,4; first bucket peaks at 24.0
        assertEquals(24.0, out[0].maxWeight, 0.0)
        assertEquals(15, out[0].totalSets)
        // Latest bar is the latest session itself, never grouped
        assertEquals(input.last(), out.last())
    }

    @Test
    fun ninety_collapsesToExactlySevenBars() {
        val input = sessions(90)
        val out = bucketSessions(input)
        assertEquals(7, out.size)
        // Even split of first 89 into 6 -> 15,15,15,15,15,14; first bucket peaks at 34.0
        assertEquals(34.0, out[0].maxWeight, 0.0)
        assertEquals(45, out[0].totalSets)
        // Latest bar is the latest session itself, never grouped
        assertEquals(input.last(), out.last())
    }

    @Test
    fun all_bundlesToAroundSevenBars() {
        val input = sessions(403, prIndex = 200)
        val out = bucketSessions(input)
        assertEquals(7, out.size)
        // PR flag propagates to its bucket
        assertEquals(1, out.count { it.isPr })
        // Latest bar is the latest session itself, never grouped
        assertEquals(input.last(), out.last())
        // peak preserved
        assertEquals(422.0, out.maxOf { it.maxWeight }, 0.0)
    }

    @Test
    fun latestBar_isNeverGrouped() {
        // Latest session dips low: it must still show its own value, not the chunk peak
        val input = sessions(30).dropLast(1) + SessionPoint(
            date = 9999L, maxWeight = 5.0, totalSets = 1, totalReps = 5,
            isPr = false, isLatest = true
        )
        val out = bucketSessions(input)
        assertEquals(7, out.size)
        assertEquals(input.last(), out.last())
        assertEquals(5.0, out.last().maxWeight, 0.0)
    }

    @Test
    fun smallLists_areNotOverBucketed() {
        assertEquals(listOf<SessionPoint>(), bucketSessions(emptyList()))
        assertEquals(5, bucketSessions(sessions(5)).size)
        assertEquals(sessions(5), bucketSessions(sessions(5)))
    }

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
    fun compareSessions_computesDeltaAndPercent() {
        val sessions = listOf(
            point(1L, weight = 80.0, volume = 800.0),
            point(2L, weight = 100.0, volume = 1200.0)
        )
        val byWeight = compareSessions(sessions, CompareMetric.MAX_WEIGHT.select)!!
        assertEquals(80.0, byWeight.initial, 0.0)
        assertEquals(100.0, byWeight.latest, 0.0)
        assertEquals(20.0, byWeight.delta, 0.0)
        assertEquals(25.0, byWeight.percent!!, 0.001)
        val byVolume = compareSessions(sessions, CompareMetric.VOLUME.select)!!
        assertEquals(800.0, byVolume.initial, 0.0)
        assertEquals(1200.0, byVolume.latest, 0.0)
        assertEquals(400.0, byVolume.delta, 0.0)
        assertEquals(50.0, byVolume.percent!!, 0.001)
    }

    @Test
    fun compareSessions_returnsNullWhenFewerThanTwo() {
        assertNull(compareSessions(emptyList(), CompareMetric.MAX_WEIGHT.select))
        assertNull(compareSessions(listOf(point(1L, weight = 80.0)), CompareMetric.MAX_WEIGHT.select))
    }

    @Test
    fun compareSessions_nullPercentWhenBaselineZero() {
        val c = compareSessions(
            listOf(point(1L, weight = 0.0), point(2L, weight = 50.0)),
            CompareMetric.MAX_WEIGHT.select
        )!!
        assertEquals(50.0, c.delta, 0.0)
        assertNull(c.percent)
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
    fun bucketing_sumsVolume() {
        val sessions = (0 until 8).map { i -> point(1000L + i, weight = 20.0 + i, volume = 100.0 * (i + 1)) }
        val out = bucketSessions(sessions)
        assertEquals(7, out.size)
        // rest = first 7 split into 6 -> 2,1,1,1,1,1; first bucket sums 100 + 200
        assertEquals(300.0, out[0].volume, 0.0)
        assertEquals(sessions.last(), out.last())
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
    fun heightLabels_mergeUnreadablyCloseValues() {
        // 1.0 and 2.0 sit within one label height of 0.0 on a 0..100 scale: single line
        assertEquals(listOf(0.0, 100.0), heightLabelValues(listOf(0.0, 1.0, 2.0, 100.0)))
    }

    @Test
    fun heightLabels_handleEdgeCases() {
        assertEquals(emptyList<Double>(), heightLabelValues(emptyList()))
        assertEquals(listOf(7.0), heightLabelValues(listOf(7.0)))
        assertEquals(listOf(50.0), heightLabelValues(listOf(50.0, 50.0, 50.0)))
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
        // Dark background (luminance < 0.45): vibrant, energetic, never washed out
        assertEquals(Color(0xFFFF5252), adaptiveRed(Color(0xFF1E1B16)))
        assertEquals(Color(0xFFFFC107), adaptiveGold(Color(0xFF1E1B16)))
        assertEquals(Color(0xFF4CAF50), adaptiveGreen(Color(0xFF1E1B16)))

        // Light background (luminance > 0.45): rich, high contrast, never muddy brown (#00504b)
        assertEquals(Color(0xFFE53935), adaptiveRed(Color(0xFFFBF8F2)))
        assertEquals(Color(0xFFD97706), adaptiveGold(Color(0xFFFBF8F2)))
        assertEquals(Color(0xFF00504B), adaptiveGreen(Color(0xFFFBF8F2)))
    }

    @Test
    fun calculateArrowDirection_returnsCorrectSlope() {
        assertEquals(ArrowDirection.UP, calculateArrowDirection(5.0))
        assertEquals(ArrowDirection.DOWN, calculateArrowDirection(-2.5))
        assertEquals(ArrowDirection.NEUTRAL, calculateArrowDirection(0.0))
        assertEquals(ArrowDirection.NEUTRAL, calculateArrowDirection(null))
    }
}



