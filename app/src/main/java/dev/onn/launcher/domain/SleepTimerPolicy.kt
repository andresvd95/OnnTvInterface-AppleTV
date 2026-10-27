package dev.onn.launcher.domain

/** Opciones del temporizador de apagado (HU-017), en minutos. */
val SLEEP_OPTIONS_MIN = listOf(15, 30, 60, 90, 120)

/** Minutos que faltan, redondeando hacia arriba; 0 si no hay temporizador o ya vencio. */
fun remainingMinutes(endAtMs: Long?, nowMs: Long): Int {
    if (endAtMs == null || endAtMs <= nowMs) return 0
    return ((endAtMs - nowMs + MINUTE - 1) / MINUTE).toInt()
}

private const val MINUTE = 60_000L