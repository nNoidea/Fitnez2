package com.nnoidea.fitnez2

import com.nnoidea.fitnez2.ui.navigation.AppPage
import org.junit.Assert.assertEquals
import org.junit.Test

class ResolveInitialRouteTest {

    @Test
    fun nullRoute_resolvesToTimeline() {
        assertEquals(AppPage.Timeline.route, resolveInitialRoute(null))
    }

    @Test
    fun knownRoute_resolvesAsIs() {
        assertEquals(AppPage.Settings.route, resolveInitialRoute("settings"))
        assertEquals(AppPage.Timeline.route, resolveInitialRoute("timeline"))
        assertEquals(AppPage.Developer.route, resolveInitialRoute("developer"))
    }

    @Test
    fun unknownRoute_resolvesToTimeline() {
        assertEquals(AppPage.Timeline.route, resolveInitialRoute("nope"))
    }
}
