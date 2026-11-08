package com.nnoidea.fitnez2

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class VersionSyncTest {

    @Test
    fun testBuildGradleMatchesVersionFile() {
        var dir: File? = File(System.getProperty("user.dir") ?: ".")
        var rootDir: File? = null
        while (dir != null) {
            if (File(dir, "VERSION").exists() && File(dir, "settings.gradle.kts").exists()) {
                rootDir = dir
                break
            }
            dir = dir.parentFile
        }

        assertTrue("Root directory with VERSION file must be found", rootDir != null && rootDir.exists())

        val versionFile = File(rootDir, "VERSION")
        val versionText = versionFile.readText().trim()
        val parts = versionText.split(".")
        assertEquals("VERSION must follow semver (e.g. 0.4.7)", 3, parts.size)

        val major = parts[0].toInt()
        val minor = parts[1].toInt()
        val patch = parts[2].toInt()
        val expectedVersionCode = major * 100_000 + minor * 100 + patch

        val buildGradleFile = File(rootDir, "app/build.gradle.kts")
        assertTrue("app/build.gradle.kts must exist", buildGradleFile.exists())
        val gradleContent = buildGradleFile.readText()

        val versionCodeMatch = Regex("""versionCode\s*=\s*(\d+)""").find(gradleContent)
        val versionNameMatch = Regex("""versionName\s*=\s*"([^"]+)"""").find(gradleContent)

        assertTrue(
            "app/build.gradle.kts must have literal integer versionCode for F-Droid",
            versionCodeMatch != null
        )
        assertTrue(
            "app/build.gradle.kts must have literal string versionName for F-Droid",
            versionNameMatch != null
        )

        val actualVersionCode = versionCodeMatch!!.groupValues[1].toInt()
        val actualVersionName = versionNameMatch!!.groupValues[1]

        assertEquals("versionCode in app/build.gradle.kts must match VERSION calculation", expectedVersionCode, actualVersionCode)
        assertEquals("versionName in app/build.gradle.kts must match VERSION", versionText, actualVersionName)
    }
}
