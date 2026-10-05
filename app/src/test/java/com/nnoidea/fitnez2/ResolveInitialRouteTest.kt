package com.nnoidea.fitnez2

import com.nnoidea.fitnez2.ui.navigation.AppPage
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveInitialRouteTest {

    @Test
    fun nullRoute_resolvesToTimeline() {
        assertEquals(AppPage.Timeline.route, resolveInitialRoute(null, developerAllowed = true))
        assertEquals(AppPage.Timeline.route, resolveInitialRoute(null, developerAllowed = false))
    }

    @Test
    fun knownRoute_resolvesAsIs() {
        assertEquals(AppPage.Settings.route, resolveInitialRoute("settings", developerAllowed = false))
        assertEquals(AppPage.Timeline.route, resolveInitialRoute("timeline", developerAllowed = false))
    }

    @Test
    fun unknownRoute_resolvesToTimeline() {
        assertEquals(AppPage.Timeline.route, resolveInitialRoute("nope", developerAllowed = true))
    }

    @Test
    fun developerRoute_allowed_resolvesToDeveloper() {
        assertEquals(AppPage.Developer.route, resolveInitialRoute("developer", developerAllowed = true))
    }

    @Test
    fun developerRoute_notAllowed_fallsBackToTimeline() {
        // Release builds do not register the developer destination; starting
        // there must not crash the NavHost.
        assertEquals(AppPage.Timeline.route, resolveInitialRoute("developer", developerAllowed = false))
    }
}
