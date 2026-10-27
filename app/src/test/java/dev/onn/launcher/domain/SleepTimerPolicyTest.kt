package dev.onn.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** QA: derivadas de HU-017 (temporizador). */
class SleepTimerPolicyTest {
    @Test fun `HU-017 opciones del temporizador`() {
        assertEquals(listOf(15, 30, 60, 90, 120), SLEEP_OPTIONS_MIN)
    }

    @Test fun `HU-017 minutos restantes redondea hacia arriba`() {
        assertEquals(30, remainingMinutes(endAtMs = 30 * 60_000L, nowMs = 0))
        assertEquals(25, remainingMinutes(endAtMs = 25 * 60_000L, nowMs = 1_000))
        assertEquals(1, remainingMinutes(endAtMs = 10_000, nowMs = 0))
    }

    @Test fun `HU-017 temporizador vencido o sin activar es 0`() {
        assertEquals(0, remainingMinutes(endAtMs = 5_000, nowMs = 9_000))
        assertEquals(0, remainingMinutes(endAtMs = null, nowMs = 9_000))
    }
}