package dev.onn.launcher.domain

/**
 * Orden de la fila de Apps (HU-018): primero las apps en el orden que eligió el usuario,
 * luego las que aún no tienen lugar (instaladas después) en orden alfabético.
 * Excluye ocultas (HU-008) y el propio launcher.
 */
fun orderApps(
    apps: List<AppEntry>,
    order: List<String>,
    hidden: Set<String>,
    selfPackage: String,
): List<AppEntry> {
    val visible = apps
        .filter { it.packageName != selfPackage && it.packageName !in hidden }
        .distinctBy { it.packageName }
    val byPackage = visible.associateBy { it.packageName }
    val placed = order.distinct().mapNotNull { byPackage[it] }
    val placedSet = placed.mapTo(HashSet()) { it.packageName }
    val rest = visible
        .filter { it.packageName !in placedSet }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    return placed + rest
}

/** Mueve un elemento una posición (delta -1 o +1). Fuera de los bordes no cambia nada. */
fun moveFavorite(list: List<String>, packageName: String, delta: Int): List<String> {
    val from = list.indexOf(packageName)
    val to = from + delta
    if (from < 0 || to !in list.indices) return list
    return list.toMutableList().apply { add(to, removeAt(from)) }
}

/** Fija una app en "Mis apps" (al final) o la quita si ya estaba. */
fun toggleFavorite(favorites: List<String>, packageName: String): List<String> =
    if (packageName in favorites) favorites - packageName else favorites + packageName
