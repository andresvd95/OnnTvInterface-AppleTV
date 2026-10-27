package dev.onn.launcher.system

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Temporizador de apagado propio (HU-017). Vive en el proceso del launcher, que se mantiene
 * activo por el servicio de accesibilidad. Al vencer, el servicio pone la TV en reposo.
 * Se cancela si la TV se reinicia.
 */
object SleepTimer {
    private val handler = Handler(Looper.getMainLooper())
    private val _endAt = MutableStateFlow<Long?>(null)

    /** Momento de apagado en reloj elapsedRealtime, o null si no hay temporizador. */
    val endAt: StateFlow<Long?> = _endAt.asStateFlow()

    /** Devuelve false si el servicio de accesibilidad no esta activo (no podria apagar la TV). */
    fun start(minutes: Int): Boolean {
        if (HomeInterceptorService.instance == null) return false
        cancel()
        val delayMs = minutes * 60_000L
        _endAt.value = SystemClock.elapsedRealtime() + delayMs
        handler.postDelayed({
            _endAt.value = null
            HomeInterceptorService.instance?.sleepNow()
        }, delayMs)
        return true
    }

    fun cancel() {
        handler.removeCallbacksAndMessages(null)
        _endAt.value = null
    }
}