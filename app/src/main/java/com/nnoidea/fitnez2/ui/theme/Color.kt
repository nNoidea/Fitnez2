package com.nnoidea.fitnez2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Light Scheme Expressive Colors
val PrimaryLight = Color(0xFF006495)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFCBE6FF)
val OnPrimaryContainerLight = Color(0xFF001E30)

val SecondaryLight = Color(0xFF006877)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFA2EEFF)
val OnSecondaryContainerLight = Color(0xFF001F25)

val TertiaryLight = Color(0xFF006B5C)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFF7CF8DF)
val OnTertiaryContainerLight = Color(0xFF00201A)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFF7F9FF)
val OnBackgroundLight = Color(0xFF181C20)
val SurfaceLight = Color(0xFFF7F9FF)
val OnSurfaceLight = Color(0xFF181C20)
val SurfaceVariantLight = Color(0xFFDDE3EA)
val OnSurfaceVariantLight = Color(0xFF41474D)
val OutlineLight = Color(0xFF71787E)
val OutlineVariantLight = Color(0xFFC1C7CE)

val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF1F4FA)
val SurfaceContainerLight = Color(0xFFEBEFF5)
val SurfaceContainerHighLight = Color(0xFFE5E9EF)
val SurfaceContainerHighestLight = Color(0xFFDFE3E9)

// Dark Scheme Expressive Colors
val PrimaryDark = Color(0xFF8FCEFF)
val OnPrimaryDark = Color(0xFF003450)
val PrimaryContainerDark = Color(0xFF004B72)
val OnPrimaryContainerDark = Color(0xFFCBE6FF)

val SecondaryDark = Color(0xFF53D7F1)
val OnSecondaryDark = Color(0xFF00363F)
val SecondaryContainerDark = Color(0xFF004E5A)
val OnSecondaryContainerDark = Color(0xFFA2EEFF)

val TertiaryDark = Color(0xFF5DDCBE)
val OnTertiaryDark = Color(0xFF00382E)
val TertiaryContainerDark = Color(0xFF005144)
val OnTertiaryContainerDark = Color(0xFF7CF8DF)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF101418)
val OnBackgroundDark = Color(0xFFDFE3E9)
val SurfaceDark = Color(0xFF101418)
val OnSurfaceDark = Color(0xFFDFE3E9)
val SurfaceVariantDark = Color(0xFF41474D)
val OnSurfaceVariantDark = Color(0xFFC1C7CE)
val OutlineDark = Color(0xFF8B9198)
val OutlineVariantDark = Color(0xFF41474D)

val SurfaceContainerLowestDark = Color(0xFF0B0F12)
val SurfaceContainerLowDark = Color(0xFF181C20)
val SurfaceContainerDark = Color(0xFF1C2024)
val SurfaceContainerHighDark = Color(0xFF272A2F)
val SurfaceContainerHighestDark = Color(0xFF31353A)

// Expressive Swipe-to-Delete Action Colors
val SwipeDeleteContainer = Color(0xFFF97386)
val OnSwipeDeleteContainer = Color(0xFF490013)

// Adaptive Semantic Colors (dynamically high-contrast against backgrounds)
val AdaptiveRedLight = Color(0xFFE53935)
val AdaptiveRedDark = Color(0xFFFF5252)

val AdaptiveGoldLight = Color(0xFFD97706)
val AdaptiveGoldDark = Color(0xFFFFC107)

val AdaptiveGreenLight = Color(0xFF008000)
val AdaptiveGreenDark = Color(0xFF4CAF50)

fun contrastRatio(c1: Color, c2: Color): Float {
    val l1 = c1.luminance()
    val l2 = c2.luminance()
    return (maxOf(l1, l2) + 0.05f) / (minOf(l1, l2) + 0.05f)
}

fun isLightBackground(background: Color): Boolean = background.luminance() > 0.20f

fun adaptiveRed(background: Color): Color =
    if (isLightBackground(background)) AdaptiveRedLight else AdaptiveRedDark

fun adaptiveGold(background: Color): Color =
    if (isLightBackground(background)) AdaptiveGoldLight else AdaptiveGoldDark

fun adaptiveGreen(background: Color): Color =
    if (isLightBackground(background)) AdaptiveGreenLight else AdaptiveGreenDark

// Alternating Record List Colors
val ColorRecordNeutralContainer: Color @Composable get() = MaterialTheme.colorScheme.primary
val ColorRecordNeutralContent: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
val ColorRecordColoredContainer: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
val ColorRecordColoredContent: Color @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer
