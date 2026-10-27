package dev.onn.launcher.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import dev.onn.launcher.domain.UsageRecord
import java.util.concurrent.TimeUnit

/** Ultimos usos de apps (HU-010). Sin permiso de estadisticas devuelve vacio y la fila no aparece. */
class RecentsRepository(private val context: Context) {

    fun hasPermission(): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java)
        val mode = ops.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun usage(): List<UsageRecord> {
        if (!hasPermission()) return emptyList()
        val usm = context.getSystemService(UsageStatsManager::class.java)
        val end = System.currentTimeMillis()
        val start = end - TimeUnit.DAYS.toMillis(LOOKBACK_DAYS)
        return usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, start, end)
            .filter { it.lastTimeUsed > 0 }
            .map { UsageRecord(it.packageName, it.lastTimeUsed) }
    }

    private companion object { const val LOOKBACK_DAYS = 14L }
}