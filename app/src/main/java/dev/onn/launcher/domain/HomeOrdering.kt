package dev.onn.launcher.domain

/**
 * Orden de la fila de Apps (HU-001, HU-006, HU-008): favoritas primero en su orden guardado,
 * luego el resto en orden alfabetico. Excluye ocultas y el propio launcher.
 */
fun orderApps(
    apps: List<AppEntry>,
    favorites: List<String>,
    hidden: Set<String>,
    selfPackage: String,
): List<AppEntry> {
    val visible = apps
        .filter { it.packageName != selfPackage && it.packageName !in hidden }
        .distinctBy { it.packageName }
    val byPackage = visible.associateBy { it.packageName }
    val pinned = favorites.distinct().mapNotNull { byPackage[it] }
    val pinnedSet = pinned.mapTo(HashSet()) { it.packageName }
    val rest = visible
        .filter { it.packageName !in pinnedSet }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    return pinned + rest
}

/** Mueve una favorita una posicion (delta -1 o +1). Fuera de los bordes no cambia nada. */
fun moveFavorite(favorites: List<String>, packageName: String, delta: Int): List<String> {
    val from = favorites.indexOf(packageName)
    val to = from + delta
    if (from < 0 || to !in favorites.indices) return favorites
    return favorites.toMutableList().apply { add(to, removeAt(from)) }
}

/** Fija una app arriba (al final de las favoritas) o la quita si ya estaba. */
fun toggleFavorite(favorites: List<String>, packageName: String): List<String> =
    if (packageName in favorites) favorites - packageName else favorites + packageName