package dev.onn.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** QA: derivadas de HU-010 (fila de Recientes). */
class RecentsPolicyTest {
    private val launchable = setOf("yt", "disney", "spotify", "kick", "settings")
    private fun u(pkg: String, t: Long) = UsageRecord(pkg, t)

    @Test fun `HU-010 ordena de la mas reciente a la mas antigua`() {
        val r = selectRecents(listOf(u("spotify", 10), u("yt", 30), u("disney", 20)), launchable, emptySet())
        assertEquals(listOf("yt", "disney", "spotify"), r)
    }

    @Test fun `HU-010 sin uso registrado la fila queda vacia`() {
        assertTrue(selectRecents(emptyList(), launchable, emptySet()).isEmpty())
    }

    @Test fun `HU-010 excluye launcher, ajustes y ocultas`() {
        val r = selectRecents(listOf(u("settings", 50), u("kick", 40), u("yt", 30)), launchable, excluded = setOf("settings", "kick"))
        assertEquals(listOf("yt"), r)
    }

    @Test fun `HU-010 una app desinstalada desaparece`() {
        val r = selectRecents(listOf(u("borrada", 99), u("yt", 1)), launchable, emptySet())
        assertEquals(listOf("yt"), r)
    }

    @Test fun `HU-010 maximo 10 y sin duplicados`() {
        val many = (1..15).map { "app$it" }
        val usage = many.map { u(it, it.removePrefix("app").toLong()) } + u("app15", 1)
        val r = selectRecents(usage, many.toSet(), emptySet())
        assertEquals(10, r.size)
        assertEquals("app15", r.first())
        assertEquals(r.distinct(), r)
    }
}
