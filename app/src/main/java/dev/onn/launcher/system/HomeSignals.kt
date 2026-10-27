package dev.onn.launcher.system

/** Marcas de tiempo compartidas entre el servicio y la pantalla para decidir reintentos. */
object HomeSignals {
    @Volatile var lastInterceptMs: Long = 0
    @Volatile var lastResumeMs: Long = 0
}