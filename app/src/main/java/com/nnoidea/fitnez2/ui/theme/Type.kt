@file:OptIn(ExperimentalTextApi::class)

package com.nnoidea.fitnez2.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.unit.sp
import com.nnoidea.fitnez2.R

// Define Google Sans Flex with the ROND axis set to 100f (fully rounded)
val GoogleSansFlexRounded = FontFamily(
    // Regular Weight (400)
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 100f),
            FontVariation.weight(400)
        )
    ),
    // Medium Weight (500)
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 100f),
            FontVariation.weight(500)
        )
    ),
    // Bold Weight (700)
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("ROND", 100f),
            FontVariation.weight(700)
        )
    )
)

// Special styled Google Sans Flex font for the App Title
val AppTitleFontFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("slnt", -10f),
            FontVariation.Setting("wdth", 142.4f),
            FontVariation.Setting("wght", 1000f),
            FontVariation.Setting("GRAD", 100f),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

private val defaultTypography = Typography()

// Default uncustomized System Typography
val SystemTypography = defaultTypography

// Apply GoogleSansFlexRounded to all 15 Material 3 Typography styles
val RoundedTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = GoogleSansFlexRounded),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = GoogleSansFlexRounded),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = GoogleSansFlexRounded),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = GoogleSansFlexRounded),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = GoogleSansFlexRounded),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = GoogleSansFlexRounded),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = GoogleSansFlexRounded),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = GoogleSansFlexRounded),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = GoogleSansFlexRounded),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = GoogleSansFlexRounded),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = GoogleSansFlexRounded),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = GoogleSansFlexRounded),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = GoogleSansFlexRounded),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = GoogleSansFlexRounded),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = GoogleSansFlexRounded)
)