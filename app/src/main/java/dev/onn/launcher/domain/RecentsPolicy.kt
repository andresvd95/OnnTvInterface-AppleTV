package dev.onn.launcher.domain

const val MAX_RECENTS = 10

/**
 * Fila de Recientes (HU-010): de la mas reciente a la mas antigua, solo apps lanzables,
 * sin excluidas (launcher, Ajustes, ocultas), sin duplicados y como maximo [max].
 */
fun selectRecents(
    usage: List<UsageRecord>,
    launchable: Set<String>,
    excluded: Set<String>,
    max: Int = MAX_RECENTS,
): List<String> = usage
    .filter { it.packageName in launchable && it.packageName !in excluded }
    .sortedByDescending { it.lastUsedMs }
    .map { it.packageName }
    .distinct()
    .take(max)