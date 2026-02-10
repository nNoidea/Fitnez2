package com.nnoidea.fitnez2.data

import androidx.compose.runtime.compositionLocalOf

val LocalAppDatabase = compositionLocalOf<AppDatabase> { error("No AppDatabase provided") }
val LocalSettingsRepository = compositionLocalOf<SettingsRepository> { error("No SettingsRepository provided") }
