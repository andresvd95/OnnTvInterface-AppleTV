package dev.onn.launcher.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import dev.onn.launcher.domain.AppEntry
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Apps lanzables en la TV y sus banners (TDD 4: AppRepository). */
class AppRepository(private val context: Context) {
    private val pm = context.packageManager

    fun launchableApps(): List<AppEntry> {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        return pm.queryIntentActivities(query, 0)
            .map { AppEntry(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
            .distinctBy { it.packageName }
    }

    /** Emite al iniciar y cada vez que se instala, quita o actualiza una app (HU-003, criterio 3). */
    fun changes(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) { trySend(Unit) }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        trySend(Unit)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    /** Banner de TV si la app lo tiene; si no, su icono. Devuelve null si la app ya no existe. */
    fun visual(packageName: String): AppVisual? = try {
        val banner = pm.getApplicationBanner(packageName)
        if (banner != null) AppVisual(banner, isBanner = true)
        else AppVisual(pm.getApplicationIcon(packageName), isBanner = false)
    } catch (e: Exception) {
        null
    }

    fun launchIntent(packageName: String): Intent? =
        pm.getLeanbackLaunchIntentForPackage(packageName) ?: pm.getLaunchIntentForPackage(packageName)
}

data class AppVisual(val drawable: Drawable, val isBanner: Boolean)