package dev.onn.launcher.system

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import dev.onn.launcher.domain.HomeKeyGate
import dev.onn.launcher.domain.needsRetry
import dev.onn.launcher.ui.HomeActivity

/**
 * ADR-002 (B reforzada): Google TV sigue activo y este servicio
 * 1) atrapa la tecla Home antes de que se dibuje Google TV,
 * 2) si aun asi se abre su pantalla de inicio, trae la nuestra al frente,
 * 3) reintenta si a los 500 ms la nuestra no quedo arriba.
 * Tambien abre el panel de notificaciones (HU-013). No lee contenido de pantalla.
 */
class HomeInterceptorService : AccessibilityService() {
    private val handler = Handler(Looper.getMainLooper())
    private val keyGate = HomeKeyGate(KEY_WINDOW_MS)

    override fun onServiceConnected() { instance = this }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        handler.removeCallbacksAndMessages(null)
        return super.onUnbind(intent)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode != KeyEvent.KEYCODE_HOME) return false
        if (event.action == KeyEvent.ACTION_UP && keyGate.shouldHandle(SystemClock.uptimeMillis())) {
            intercept()
        }
        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        if (event.className?.toString() == STOCK_HOME_ACTIVITY) intercept()
    }

    override fun onInterrupt() = Unit

    fun openNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    private fun intercept() {
        HomeSignals.lastInterceptMs = SystemClock.uptimeMillis()
        bringLauncherToFront()
        scheduleCheck(attempt = 1)
    }

    private fun bringLauncherToFront() {
        startActivity(
            Intent(this, HomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
        )
    }

    private fun scheduleCheck(attempt: Int) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (needsRetry(HomeSignals.lastResumeMs, HomeSignals.lastInterceptMs)) {
                bringLauncherToFront()
                if (attempt < MAX_RETRIES) scheduleCheck(attempt + 1)
            }
        }, RETRY_DELAY_MS)
    }

    companion object {
        const val STOCK_HOME_ACTIVITY = "com.google.android.apps.tv.launcherx.home.HomeActivity"
        private const val KEY_WINDOW_MS = 300L
        private const val RETRY_DELAY_MS = 500L
        private const val MAX_RETRIES = 3

        @Volatile var instance: HomeInterceptorService? = null
            private set
    }
}