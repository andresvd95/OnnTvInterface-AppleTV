package dev.onn.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** QA: derivadas de HU-014 y HU-020 (portada). */
class HeroPolicyTest {
    private fun app(p: String) = AppEntry(p, p)
    private val min = 60_000L
    private val hour = 60 * min
    private val day = 24 * hour

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

    private fun m(p: String, t: Long) = MediaItem(p, "Titulo $p", null, null, t)

    @Test fun `HU-020 la app con foco muestra su contenido si lo tiene`() {
        val r = pickHero(app("netflix"), listOf(app("netflix")), mapOf("netflix" to m("netflix", 5)))
        assertTrue(r is HeroContent.Continue)
    }

    @Test fun `HU-020 la app con foco sin contenido se muestra en grande`() {
        val r = pickHero(app("yt"), listOf(app("yt")), emptyMap())
        assertEquals(HeroContent.App(app("yt")), r)
    }

    @Test fun `HU-020 sin foco en las apps muestra lo ultimo reproducido`() {
        val apps = listOf(app("a"), app("b"), app("c"))
        val r = pickHero(null, apps, mapOf("b" to m("b", 10), "c" to m("c", 20)))
        assertEquals("c", (r as HeroContent.Continue).app.packageName)
    }

    @Test fun `HU-020 sin contenido muestra la primera app del orden`() {
        assertEquals(HeroContent.App(app("a")), pickHero(null, listOf(app("a"), app("b")), emptyMap()))
        assertNull(pickHero(null, emptyList(), emptyMap()))
    }

    @Test fun `HU-020 descarta contenido viejo y de apps ocultas o desinstaladas`() {
        val now = 30 * day
        val r = freshMedia(
            listOf(m("a", now - 8 * day), m("b", now - hour), m("oculta", now)),
            visible = setOf("a", "b"), nowMs = now,
        )
        assertEquals(setOf("b"), r.keys)
    }

    @Test fun `HU-020 si una app tiene varios registros gana el mas nuevo`() {
        val r = freshMedia(listOf(m("a", 1), m("a", 9)), setOf("a"), nowMs = 10)
        assertEquals(9L, r.getValue("a").updatedMs)
    }
}
