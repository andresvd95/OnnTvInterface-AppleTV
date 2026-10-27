package dev.onn.launcher.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings

/** Accesos de la barra superior (HU-004, HU-011, HU-012, HU-013). Devuelven false si no se pudo abrir. */
class SystemShortcuts(private val context: Context) {

    fun openSettings(): Boolean = start(Intent(Settings.ACTION_SETTINGS))

    fun openPlay(): Boolean {
        val pm = context.packageManager
        val intent = pm.getLeanbackLaunchIntentForPackage(PLAY) ?: pm.getLaunchIntentForPackage(PLAY)
        return intent != null && start(intent)
    }

    /** Selector de perfiles de Google TV (verificado en la PoC). */
    fun openProfiles(): Boolean =
        start(Intent().setComponent(ComponentName(GOOGLE_TV, "$GOOGLE_TV.profile.chooser.ProfileChooserActivity")))

    /** El intent directo esta prohibido para apps normales; se usa el servicio de accesibilidad. */
    fun openNotifications(): Boolean = HomeInterceptorService.instance?.openNotifications() ?: false

    fun start(intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: Exception) {
        false
    }

    private companion object {
        const val PLAY = "com.android.vending"
        const val GOOGLE_TV = "com.google.android.apps.tv.launcherx"
    }
}