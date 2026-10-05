package com.nnoidea.fitnez2.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FontSystemTest {

    @Test
    fun roundedTypography_usesRoundedFontForAllStyles() {
        val styles = listOf(
            RoundedTypography.displayLarge,
            RoundedTypography.displayMedium,
            RoundedTypography.displaySmall,
            RoundedTypography.headlineLarge,
            RoundedTypography.headlineMedium,
            RoundedTypography.headlineSmall,
            RoundedTypography.titleLarge,
            RoundedTypography.titleMedium,
            RoundedTypography.titleSmall,
            RoundedTypography.bodyLarge,
            RoundedTypography.bodyMedium,
            RoundedTypography.bodySmall,
            RoundedTypography.labelLarge,
            RoundedTypography.labelMedium,
            RoundedTypography.labelSmall
        )
        styles.forEach { style ->
            assertEquals(GoogleSansFlexRounded, style.fontFamily)
        }
    }

    @Test
    fun systemTypography_doesNotUseRoundedFont() {
        val styles = listOf(
            SystemTypography.displayLarge,
            SystemTypography.displayMedium,
            SystemTypography.headlineLarge,
            SystemTypography.titleMedium,
            SystemTypography.bodyLarge,
            SystemTypography.bodyMedium,
            SystemTypography.labelSmall
        )
        styles.forEach { style ->
            assertNotEquals(GoogleSansFlexRounded, style.fontFamily)
        }
    }
}
