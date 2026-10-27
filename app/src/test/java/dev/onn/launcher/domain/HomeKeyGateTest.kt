package dev.onn.launcher.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** QA: derivadas de HU-003 y del informe de PoC (pulsaciones rapidas, reintento). */
class HomeKeyGateTest {
    @Test fun `HU-003 la primera pulsacion siempre se atiende`() {
        assertTrue(HomeKeyGate(windowMs = 300).shouldHandle(nowMs = 1_000))
    }

    @Test fun `HU-003 un aviso duplicado dentro de la ventana se ignora`() {
        val g = HomeKeyGate(300)
        g.shouldHandle(1_000)
        assertFalse(g.shouldHandle(1_150))
    }

    @Test fun `HU-003 una pulsacion nueva fuera de la ventana se atiende`() {
        val g = HomeKeyGate(300)
        g.shouldHandle(1_000)
        assertTrue(g.shouldHandle(1_400))
    }

    @Test fun `HU-003 hay que reintentar si el launcher no volvio despues de interceptar`() {
        assertTrue(needsRetry(lastResumeMs = 900, lastInterceptMs = 1_000))
        assertFalse(needsRetry(lastResumeMs = 1_100, lastInterceptMs = 1_000))
    }
}
