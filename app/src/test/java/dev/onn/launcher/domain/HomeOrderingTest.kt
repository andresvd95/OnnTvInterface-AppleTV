package dev.onn.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** QA: derivadas de HU-001, HU-006 y HU-008 (docs/03-historias.md). */
class HomeOrderingTest {
    private val self = "dev.onn.launcher"
    private fun app(pkg: String, label: String) = AppEntry(pkg, label)
    private val installed = listOf(
        app("com.spotify", "Spotify"),
        app("com.disney", "Disney+"),
        app("com.youtube", "YouTube"),
        app(self, "ONN Launcher"),
        app("com.amazon", "amazon"),
    )

    @Test fun `HU-001 sin favoritas las apps van en orden alfabetico sin distinguir mayusculas`() {
        val r = orderApps(installed, favorites = emptyList(), hidden = emptySet(), selfPackage = self)
        assertEquals(listOf("amazon", "Disney+", "Spotify", "YouTube"), r.map { it.label })
    }

    @Test fun `HU-001 el propio launcher nunca aparece en la fila`() {
        val r = orderApps(installed, emptyList(), emptySet(), self)
        assertFalse(r.any { it.packageName == self })
    }

    @Test fun `HU-006 las favoritas van primero y en su orden guardado`() {
        val r = orderApps(installed, favorites = listOf("com.youtube", "com.spotify"), hidden = emptySet(), selfPackage = self)
        assertEquals(listOf("YouTube", "Spotify", "amazon", "Disney+"), r.map { it.label })
    }

    @Test fun `HU-006 una favorita desinstalada se ignora sin error`() {
        val r = orderApps(installed, favorites = listOf("com.borrada", "com.disney"), hidden = emptySet(), selfPackage = self)
        assertEquals("Disney+", r.first().label)
        assertEquals(4, r.size)
    }

    @Test fun `HU-008 las apps ocultas no se muestran aunque sean favoritas`() {
        val r = orderApps(installed, favorites = listOf("com.disney"), hidden = setOf("com.disney"), selfPackage = self)
        assertFalse(r.any { it.packageName == "com.disney" })
    }

    @Test fun `HU-006 mover a la derecha intercambia con la vecina`() {
        assertEquals(listOf("b", "a", "c"), moveFavorite(listOf("a", "b", "c"), "a", +1))
    }

    @Test fun `HU-006 mover fuera del borde no cambia nada`() {
        assertEquals(listOf("a", "b", "c"), moveFavorite(listOf("a", "b", "c"), "a", -1))
        assertEquals(listOf("a", "b", "c"), moveFavorite(listOf("a", "b", "c"), "c", +1))
    }

    @Test fun `HU-006 fijar arriba agrega al final y quitar la saca`() {
        assertEquals(listOf("a", "b"), toggleFavorite(listOf("a"), "b"))
        assertEquals(listOf("b"), toggleFavorite(listOf("a", "b"), "a"))
    }
}
