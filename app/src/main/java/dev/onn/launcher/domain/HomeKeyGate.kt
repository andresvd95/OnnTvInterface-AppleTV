package dev.onn.launcher.domain

/**
 * Evita atender dos veces la misma pulsacion de Home (el sistema puede avisar en rafaga).
 * Ver docs/06-poc-informe.md.
 */
class HomeKeyGate(private val windowMs: Long = 300) {
    private var lastHandledMs: Long? = null

    fun shouldHandle(nowMs: Long): Boolean {
        val last = lastHandledMs
        if (last != null && nowMs - last < windowMs) return false
        lastHandledMs = nowMs
        return true
    }
}

/** Reintento (ADR-002): si el launcher no volvio a primer plano despues de interceptar Home. */
fun needsRetry(lastResumeMs: Long, lastInterceptMs: Long): Boolean = lastResumeMs < lastInterceptMs