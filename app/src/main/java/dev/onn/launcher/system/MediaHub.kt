package dev.onn.launcher.system

import android.content.Context
import android.graphics.Bitmap
import dev.onn.launcher.data.ContinueStore
import dev.onn.launcher.domain.MediaItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.Executors
import kotlin.math.roundToInt

/** Contenido "Continuar" compartido entre el servicio que lo captura y la pantalla (HU-020). */
object MediaHub {
    private val io = Executors.newSingleThreadExecutor()
    private val _items = MutableStateFlow<Map<String, MediaItem>>(emptyMap())
    val items: StateFlow<Map<String, MediaItem>> = _items
    @Volatile private var store: ContinueStore? = null

    private fun store(context: Context): ContinueStore =
        store ?: synchronized(this) { store ?: ContinueStore(context.applicationContext).also { store = it } }

    /** Lee lo guardado una sola vez por proceso. */
    fun load(context: Context) {
        if (store != null) return
        val s = store(context)
        io.execute { _items.update { captured -> s.loadAll().associateBy { it.packageName } + captured } }
    }

    fun record(context: Context, pkg: String, title: String, subtitle: String?, updatedMs: Long, art: () -> Bitmap?) {
        val s = store(context)
        io.execute {
            val old = _items.value[pkg]
            val sameTitle = old != null && old.title == title && old.subtitle == subtitle
            // Mismo contenido hace poco: no reescribir (cada pausa o avance avisa).
            if (sameTitle && old != null && old.artPath != null && updatedMs - old.updatedMs < MIN_REWRITE_MS) return@execute
            val bitmap = runCatching { art()?.let(::shrink) }.getOrNull()
            val saved = runCatching { s.save(MediaItem(pkg, title, subtitle, null, updatedMs), bitmap, sameTitle) }.getOrNull()
                ?: return@execute
            _items.update { it + (pkg to saved) }
        }
    }

    private fun shrink(b: Bitmap): Bitmap {
        if (b.width <= MAX_ART_W) return b
        val h = (b.height * MAX_ART_W / b.width.toFloat()).roundToInt()
        return Bitmap.createScaledBitmap(b, MAX_ART_W, h, true)
    }

    private const val MAX_ART_W = 1280
    private const val MIN_REWRITE_MS = 60_000L
}
