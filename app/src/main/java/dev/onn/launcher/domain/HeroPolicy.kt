package dev.onn.launcher.domain

const val MAX_HERO = 5

/** Tarjetas (HU-001 v0.4): primero las recientes, luego el resto de apps, sin duplicados. */
fun cardOrder(apps: List<AppEntry>, recents: List<AppEntry>): List<AppEntry> =
    (recents + apps).distinctBy { it.packageName }

/** Apps que rota la portada cuando nadie navega (HU-014). */
fun heroCandidates(recents: List<AppEntry>): List<AppEntry> = recents.take(MAX_HERO)

fun nextHeroIndex(current: Int, size: Int): Int = if (size <= 0) 0 else (current + 1) % size

/** Cuándo se usó una app por última vez, para el subtítulo de portada y tarjetas (HU-014). */
sealed interface LastUsed {
    data object New : LastUsed
    data class Minutes(val n: Long) : LastUsed
    data class Hours(val n: Long) : LastUsed
    data object Yesterday : LastUsed
    data class Days(val n: Long) : LastUsed
}

fun lastUsed(lastUsedMs: Long?, nowMs: Long): LastUsed {
    if (lastUsedMs == null || lastUsedMs <= 0) return LastUsed.New
    val diff = (nowMs - lastUsedMs).coerceAtLeast(0)
    return when {
        diff < HOUR -> LastUsed.Minutes((diff / MINUTE).coerceAtLeast(1))
        diff < DAY -> LastUsed.Hours(diff / HOUR)
        diff < 2 * DAY -> LastUsed.Yesterday
        else -> LastUsed.Days(diff / DAY)
    }
}

private const val MINUTE = 60_000L
private const val HOUR = 60 * MINUTE
private const val DAY = 24 * HOUR