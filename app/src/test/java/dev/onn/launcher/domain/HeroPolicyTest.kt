package dev.onn.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** QA: derivadas de HU-014 (portada) y HU-001/HU-010 v0.4 (tarjetas). */
class HeroPolicyTest {
    private fun app(p: String) = AppEntry(p, p)
    private val min = 60_000L
    private val hour = 60 * min
    private val day = 24 * hour

    @Test fun `HU-001 las tarjetas empiezan por las recientes y luego el resto sin duplicados`() {
        val r = cardOrder(apps = listOf(app("a"), app("b"), app("c")), recents = listOf(app("c"), app("a")))
        assertEquals(listOf("c", "a", "b"), r.map { it.packageName })
    }

    @Test fun `HU-014 app sin uso registrado es Nueva`() {
        assertEquals(LastUsed.New, lastUsed(null, nowMs = 10 * day))
    }

    @Test fun `HU-014 hace minutos, minimo 1`() {
        assertEquals(LastUsed.Minutes(1), lastUsed(10 * day - 10_000, 10 * day))
        assertEquals(LastUsed.Minutes(45), lastUsed(10 * day - 45 * min, 10 * day))
    }

    @Test fun `HU-014 hace horas`() {
        assertEquals(LastUsed.Hours(2), lastUsed(10 * day - 2 * hour, 10 * day))
    }

    @Test fun `HU-014 ayer y hace dias`() {
        assertEquals(LastUsed.Yesterday, lastUsed(10 * day - 30 * hour, 10 * day))
        assertEquals(LastUsed.Days(3), lastUsed(10 * day - 3 * day, 10 * day))
    }

    @Test fun `HU-014 el carrusel avanza y vuelve al inicio`() {
        assertEquals(1, nextHeroIndex(0, 3))
        assertEquals(0, nextHeroIndex(2, 3))
        assertEquals(0, nextHeroIndex(0, 0))
    }

    @Test fun `HU-014 el carrusel usa como maximo 5 recientes`() {
        assertEquals(5, heroCandidates((1..9).map { app("p$it") }).size)
    }
}
