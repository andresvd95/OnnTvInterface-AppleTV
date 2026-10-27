package dev.onn.launcher.domain

/** App lanzable desde la TV (categoria Leanback). */
data class AppEntry(val packageName: String, val label: String)

/** Ultimo uso conocido de una app (estadisticas de uso del sistema). */
data class UsageRecord(val packageName: String, val lastUsedMs: Long)