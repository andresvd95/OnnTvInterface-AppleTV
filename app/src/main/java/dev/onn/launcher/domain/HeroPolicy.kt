package dev.onn.launcher.domain

/** "Continuar" guarda contenido como máximo una semana (HU-020). */
const val MAX_CONTINUE_AGE_MS = 7L * 24 * 60 * 60 * 1000

/** Lo último que reprodujo una app, leído de su sesión multimedia (HU-020). */
data class MediaItem(
    val packageName: String,
    val title: String,
    val subtitle: String?,
    val artPath: String?,
    val updatedMs: Long,
)

/** Qué muestra la portada (HU-020). */
sealed interface HeroContent {
    val app: AppEntry
    /** Contenido real de la app: portada, título y botón Continuar. */
    data class Continue(override val app: AppEntry, val media: MediaItem) : HeroContent
    /** La app en grande, cuando no expone contenido. */
    data class App(override val app: AppEntry) : HeroContent
}

/** Contenido vigente por app: solo apps visibles y de la última semana. */
fun freshMedia(
    media: Collection<MediaItem>,
    visible: Set<String>,
    nowMs: Long,
    maxAgeMs: Long = MAX_CONTINUE_AGE_MS,
): Map<String, MediaItem> = media
    .filter { it.packageName in visible && nowMs - it.updatedMs <= maxAgeMs }
    .groupBy { it.packageName }
    .mapValues { (_, v) -> v.maxBy { it.updatedMs } }

/**
 * Portada (HU-020): la app con foco; si no hay foco en las apps, lo último reproducido;
 * si no hay nada reproducido, la primera app del orden del usuario.
 */
fun pickHero(focused: AppEntry?, apps: List<AppEntry>, media: Map<String, MediaItem>): HeroContent? {
    if (focused != null) {
        return media[focused.packageName]?.let { HeroContent.Continue(focused, it) } ?: HeroContent.App(focused)
    }
    val byPackage = apps.associateBy { it.packageName }
    media.values.sortedByDescending { it.updatedMs }.forEach { m ->
        byPackage[m.packageName]?.let { return HeroContent.Continue(it, m) }
    }
    return apps.firstOrNull()?.let { HeroContent.App(it) }
}

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
