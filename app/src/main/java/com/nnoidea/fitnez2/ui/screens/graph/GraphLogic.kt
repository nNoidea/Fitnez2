package com.nnoidea.fitnez2.ui.screens.graph

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.nnoidea.fitnez2.core.TimeUtils
import com.nnoidea.fitnez2.core.localization.globalLocalization
import com.nnoidea.fitnez2.data.entities.Exercise
import com.nnoidea.fitnez2.data.entities.Record
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs

/** Formats a numeric value with up to 2 decimal places, rounding to nearest hundredth. */
fun formatMaxTwoDecimals(value: Double, locale: Locale = Locale.getDefault()): String {
    if (value.isNaN() || value.isInfinite()) return "--"
    val v = if (abs(value) < 0.005) 0.0 else value
    val symbols = DecimalFormatSymbols.getInstance(locale)
    val df = DecimalFormat("0.##", symbols).apply {
        roundingMode = RoundingMode.HALF_UP
    }
    return df.format(v)
}

/** Rounds a numeric value to 2 decimal places using HALF_UP. */
fun roundToTwoDecimals(value: Double): Double {
    if (value.isNaN() || value.isInfinite()) return value
    val v = if (abs(value) < 0.005) 0.0 else value
    return java.math.BigDecimal.valueOf(v).setScale(2, java.math.RoundingMode.HALF_UP).toDouble()
}

enum class ArrowDirection { UP, DOWN, NEUTRAL }

fun calculateArrowDirection(delta: Double?): ArrowDirection = when {
    delta == null || abs(delta) < 0.005 -> ArrowDirection.NEUTRAL
    delta > 0.0 -> ArrowDirection.UP
    else -> ArrowDirection.DOWN
}

enum class SessionRange(val count: Int?, val label: String) {
    TWO(2, "2"),
    SEVEN(7, "7"),
    THIRTY(30, "30"),
    ALL(null, "ALL")
}

data class DaySession(
    val date: Long,
    val maxWeight: Double,
    val volume: Double,
    val totalSets: Int,
    val totalReps: Int,
    val oneRm: Double = 0.0
)

/**
 * What the graph compares: heaviest single weight, session volume (kg x reps x sets),
 * or weighted peak 1RM (Epley formula).
 */
enum class CompareMetric(
    val select: (SessionPoint) -> Double,
    val selectDay: (DaySession) -> Double
) {
    MAX_WEIGHT({ it.maxWeight }, { it.maxWeight }),
    VOLUME({ it.volume }, { it.volume }),
    ONE_RM({ it.oneRm }, { it.oneRm });

    fun select(day: DaySession): Double = selectDay(day)

    fun label(): String = when (this) {
        MAX_WEIGHT -> globalLocalization.labelGraphMaxWeight
        VOLUME -> globalLocalization.labelGraphVolume
        ONE_RM -> globalLocalization.labelGraphOneRm
    }
}

fun groupRecordsByDay(
    records: List<com.nnoidea.fitnez2.data.entities.Record>,
    zoneId: ZoneId = ZoneId.systemDefault(),
    rolloverHour: Int = 0
): List<DaySession> {
    if (records.isEmpty()) return emptyList()
    return records
        .groupBy { record ->
            TimeUtils.getWorkoutLocalDate(record.date, rolloverHour, zoneId)
        }
        .map { (_, dayRecords) ->
            DaySession(
                date = TimeUtils.getWorkoutEpochMillis(dayRecords.maxOf { it.date }, rolloverHour, zoneId),
                maxWeight = roundToTwoDecimals(dayRecords.maxOf { it.weight }),
                volume = roundToTwoDecimals(dayRecords.sumOf { it.weight * it.reps * it.sets }),
                totalSets = dayRecords.sumOf { it.sets },
                totalReps = dayRecords.sumOf { it.reps * it.sets },
                oneRm = roundToTwoDecimals(calculateDayOneRm(dayRecords))
            )
        }
        .sortedBy { it.date }
}

fun buildChartBars(
    daySessions: List<DaySession>,
    range: SessionRange,
    metric: CompareMetric
): List<SessionPoint> {
    if (daySessions.isEmpty()) return emptyList()

    val days = when (range) {
        SessionRange.TWO -> daySessions.takeLast(2)
        SessionRange.SEVEN -> daySessions.takeLast(8)
        SessionRange.THIRTY -> daySessions.takeLast(30)
        SessionRange.ALL -> daySessions
    }

    if (days.size == 1) {
        val d = days.first()
        val v = metric.select(d)
        return listOf(
            SessionPoint(
                date = d.date,
                maxWeight = d.maxWeight,
                totalSets = d.totalSets,
                totalReps = d.totalReps,
                volume = d.volume,
                oneRm = d.oneRm,
                isPr = false,
                isLatest = true,
                leftValue = v,
                rightValue = v
            )
        )
    }

    if (days.size <= 8) {
        val lastIdx = days.lastIndex
        return (0 until lastIdx).map { i ->
            val prevDay = days[i]
            val currDay = days[i + 1]
            val leftVal = metric.select(prevDay)
            val rightVal = metric.select(currDay)
            SessionPoint(
                date = currDay.date,
                maxWeight = currDay.maxWeight,
                totalSets = currDay.totalSets,
                totalReps = currDay.totalReps,
                volume = currDay.volume,
                oneRm = currDay.oneRm,
                isPr = false,
                isLatest = (i == lastIdx - 1),
                leftValue = leftVal,
                rightValue = rightVal,
                startDate = prevDay.date
            )
        }
    }

    val latestDay = days.last()
    val rest = days.dropLast(1)
    val base = rest.size / 6
    var remainder = rest.size % 6
    var from = 0

    val chunks = (0 until 6).map {
        val size = base + if (remainder > 0) {
            remainder--
            1
        } else 0
        val chunk = rest.subList(from, from + size)
        from += size
        chunk
    }

    val c0 = metric.select(rest.first())
    val bucketMaxes = chunks.map { chunk -> chunk.maxOf { metric.select(it) } }
    val cLatest = metric.select(latestDay)
    val corners = listOf(c0) + bucketMaxes + listOf(cLatest)

    val bucketBars = chunks.mapIndexed { index, chunk ->
        SessionPoint(
            date = chunk.last().date,
            maxWeight = chunk.maxOf { it.maxWeight },
            totalSets = chunk.sumOf { it.totalSets },
            totalReps = chunk.sumOf { it.totalReps },
            volume = chunk.maxOf { it.volume },
            oneRm = chunk.maxOf { it.oneRm },
            isPr = false,
            isLatest = false,
            leftValue = corners[index],
            rightValue = corners[index + 1],
            startDate = chunk.first().date
        )
    }

    val latestBar = SessionPoint(
        date = latestDay.date,
        maxWeight = latestDay.maxWeight,
        totalSets = latestDay.totalSets,
        totalReps = latestDay.totalReps,
        volume = latestDay.volume,
        oneRm = latestDay.oneRm,
        isPr = false,
        isLatest = true,
        leftValue = corners[6],
        rightValue = corners[7],
        startDate = latestDay.date
    )

    return bucketBars + latestBar
}

/** Initial -> latest comparison over the visible window; null when there is no pair. */
data class MetricComparison(
    val initial: Double,
    val latest: Double,
    val delta: Double,
    val percent: Double?
)

/**
 * Spotlight the peak session only when it is distinct (> min and > 0).
 * Ties resolve to the most recent session.
 */
fun markPrFlags(
    sessions: List<SessionPoint>,
    select: (SessionPoint) -> Double = { it.rightValue }
): List<SessionPoint> {
    if (sessions.isEmpty()) return emptyList()
    val max = sessions.maxOf(select)
    val min = sessions.minOf(select)
    // ponytail: O(n) scans x4 over a tiny in-memory list; a single pass saves nothing here.
    val prDate = if (max > 0.0 && max > min) {
        sessions.filter { select(it) == max }.maxByOrNull { it.date }?.date
    } else null
    return sessions.map { it.copy(isPr = it.date == prDate) }
}

fun resolveDefaultExerciseId(
    exercises: List<com.nnoidea.fitnez2.data.entities.Exercise>,
    latestRecordExerciseId: String?
): String? {
    if (exercises.isEmpty()) return null
    if (latestRecordExerciseId != null && exercises.any { it.id == latestRecordExerciseId }) {
        return latestRecordExerciseId
    }
    return exercises.first().id
}

data class SessionPoint(
    val date: Long,
    val maxWeight: Double,
    val totalSets: Int,
    val totalReps: Int,
    val volume: Double = 0.0,
    val oneRm: Double = 0.0,
    val isPr: Boolean = false,
    val isLatest: Boolean = false,
    val leftValue: Double = maxWeight,
    val rightValue: Double = maxWeight,
    val startDate: Long = date
)

/**
 * Rounded-corner polygon: every vertex is trimmed along both adjacent edges and
 * rejoined with a quadratic, so all corners share one consistent radius.
 * Pure math over [Offset]s so it stays unit-testable; the Canvas just replays it.
 */
data class CornerOp(val lineTo: Offset, val control: Offset, val end: Offset)
data class RoundedPolygon(val start: Offset, val ops: List<CornerOp>)

fun roundedPolygon(points: List<Offset>, radius: Float): RoundedPolygon {
    data class Trim(val a: Offset, val p: Offset, val b: Offset)
    val trims = points.mapIndexed { i, p ->
        val prev = points[(i - 1 + points.size) % points.size]
        val next = points[(i + 1) % points.size]
        val ePrev = p - prev
        val eNext = next - p
        val lenPrev = ePrev.getDistance()
        val lenNext = eNext.getDistance()
        val r = minOf(radius, lenPrev / 2f, lenNext / 2f).coerceAtLeast(0f)
        val a = if (lenPrev > 0f && r > 0f) p - ePrev / lenPrev * r else p
        val b = if (lenNext > 0f && r > 0f) p + eNext / lenNext * r else p
        Trim(a, p, b)
    }
    val ordered = trims.drop(1) + trims.take(1)
    return RoundedPolygon(
        start = trims[0].b,
        ops = ordered.map { CornerOp(it.a, it.p, it.b) }
    )
}

/**
 * Height labels in data units: preserves all distinct values (one per distinct height),
 * sorted ascending. Equal values collapse to share a single line.
 */
fun heightLabelValues(values: List<Double>): List<Double> {
    if (values.isEmpty()) return emptyList()
    // Deduplicate by formatted 2-decimal string so the Y-axis never renders two lines with the exact same display label.
    // If multiple values share the same formatted label, keep the max value to preserve upper bounds / PR pill.
    return values.sorted()
        .groupBy { formatMaxTwoDecimals(it) }
        .map { (_, group) -> group.max() }
        .sorted()
}

/**
 * Resolves a raw data value to its canonical height label value.
 * If multiple values round to the same display string, they resolve to the same canonical label
 * so they are graphed at the exact same height on their shared gridline.
 */
fun resolveCanonicalValue(value: Double, labelValues: List<Double>): Double {
    return labelValues.firstOrNull { formatMaxTwoDecimals(it) == formatMaxTwoDecimals(value) } ?: value
}

/**
 * Computes non-overlapping Y pixel coordinates for distinct chart values.
 * Anchors the max value at [yTop] and min value at [yBottom].
 * If any adjacent values would sit closer than [minGapPx], enforces at least [minGapPx]
 * between their horizontal lines so they are neatly stacked under each other without overlapping.
 */
fun computeNonOverlappingY(
    distinctValues: List<Double>,
    yTop: Float,
    yBottom: Float,
    minGapPx: Float
): Map<Double, Float> {
    if (distinctValues.isEmpty()) return emptyMap()
    if (distinctValues.size == 1) return mapOf(distinctValues.first() to yTop)

    val n = distinctValues.size
    val minVal = distinctValues.first()
    val maxVal = distinctValues.last()
    val valRange = maxVal - minVal

    // Proportional ideal positions: highest value (index n-1) at yTop, lowest at yBottom
    val idealY = FloatArray(n) { i ->
        if (valRange <= 0.0) yTop
        else {
            val fraction = ((distinctValues[i] - minVal) / valRange).toFloat()
            yBottom - fraction * (yBottom - yTop)
        }
    }

    val effectiveMinGap = minOf(minGapPx, (yBottom - yTop) / (n - 1).coerceAtLeast(1))
    val y = idealY.clone()

    // Anchor bounds
    y[n - 1] = yTop
    y[0] = yBottom

    // Top-down pass: ensure each lower value has at least effectiveMinGap distance below the higher value
    for (i in (n - 2) downTo 1) {
        val minAllowedY = y[i + 1] + effectiveMinGap
        if (y[i] < minAllowedY) {
            y[i] = minAllowedY
        }
    }

    // Bottom-up pass: if pushed too close to bottom or next lower value, push back up
    for (i in 1 until (n - 1)) {
        val maxAllowedY = y[i - 1] - effectiveMinGap
        if (y[i] > maxAllowedY) {
            y[i] = maxAllowedY
        }
    }

    // Secondary top-down pass to resolve any residual compression
    for (i in (n - 2) downTo 1) {
        val minAllowedY = y[i + 1] + effectiveMinGap
        if (y[i] < minAllowedY) {
            y[i] = minAllowedY
        }
    }

    return distinctValues.indices.associate { i -> distinctValues[i] to y[i] }
}

/**
 * Silhouette corner heights in data units: the first bar's top-left plus every
 * bar's top-right. Labels drawn from these always sit exactly on the drawn silhouette.
 */
fun silhouetteCornerValues(bars: List<SessionPoint>): List<Double> {
    if (bars.isEmpty()) return emptyList()
    return listOf(bars.first().leftValue) + bars.map { it.rightValue }
}

/** Pill backdrop for an on-graph axis label: hugs right-aligned text with padding. */
data class LabelPillRect(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val height: Float get() = bottom - top
}

/** One label per silhouette corner: the first bar's left edge plus every bar's right edge. */
data class CornerLabelPoint(val value: Double, val x: Float)

/**
 * Collapses back-to-back corners sharing the same label, keeping the latest bar's
 * position — runs like 1,1,1,3,3,3 draw as 1,3. Separated repeats are kept.
 */
fun collapseConsecutiveLabels(points: List<CornerLabelPoint>): List<CornerLabelPoint> {
    if (points.size < 2) return points
    return buildList {
        points.forEachIndexed { i, point ->
            if (i == points.lastIndex || points[i + 1].value != point.value) add(point)
        }
    }
}

/**
 * Positions for all corner labels: first bar's left edge, junction centers
 * between bars, and the last bar's right edge. Inner corners share their value
 * with the next bar's left, so labeling every right corner covers them.
 */
fun cornerLabelPositions(
    bars: List<SessionPoint>,
    paddingLeft: Float,
    slotWidth: Float,
    gap: Float
): List<CornerLabelPoint> {
    if (bars.isEmpty()) return emptyList()
    return buildList {
        add(CornerLabelPoint(bars.first().leftValue, paddingLeft + gap / 2f))
        for (k in 1 until bars.size) {
            add(CornerLabelPoint(bars[k - 1].rightValue, paddingLeft + k * slotWidth))
        }
        add(CornerLabelPoint(bars.last().rightValue, paddingLeft + bars.size * slotWidth - gap / 2f))
    }
}

/** Pill backdrop centered over [centerX], floating [liftGap] above [lineY], shifted to stay inside [minLeft, maxRight]. */
fun centeredLabelPillRect(
    textWidth: Float,
    ascent: Float,
    descent: Float,
    centerX: Float,
    lineY: Float,
    liftGap: Float,
    paddingH: Float,
    paddingV: Float,
    minLeft: Float,
    maxRight: Float
): LabelPillRect {
    val baselineY = lineY - liftGap - paddingV - descent
    val pillWidth = textWidth + 2 * paddingH
    val left = (centerX - pillWidth / 2f).coerceIn(minLeft, (maxRight - pillWidth).coerceAtLeast(minLeft))
    return LabelPillRect(
        left = left,
        top = baselineY + ascent - paddingV,
        right = left + pillWidth,
        bottom = baselineY + descent + paddingV
    )
}
