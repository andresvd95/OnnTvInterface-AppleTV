package dev.onn.launcher.data

import android.content.Context
import android.graphics.Bitmap
import dev.onn.launcher.domain.MediaItem
import java.io.File
import java.util.Properties

/**
 * "Continuar" (HU-020): lo último que reprodujo cada app, guardado solo en la TV
 * (filesDir/continue): un .properties con título y fecha, y un .jpg con la portada.
 */
class ContinueStore(context: Context) {
    private val dir = File(context.filesDir, DIR).apply { mkdirs() }

    fun loadAll(): List<MediaItem> = dir.listFiles { f -> f.extension == META }.orEmpty().mapNotNull { f ->
        runCatching {
            val p = Properties().apply { f.inputStream().use { load(it) } }
            val pkg = f.nameWithoutExtension
            val title = p.getProperty(TITLE) ?: return@runCatching null
            val art = File(dir, "$pkg.$ART").takeIf { it.exists() }?.absolutePath
            MediaItem(pkg, title, p.getProperty(SUBTITLE), art, p.getProperty(UPDATED).toLong())
        }.getOrNull()
    }

    /** Guarda el contenido de una app. Sin imagen nueva conserva la anterior solo si es el mismo título. */
    fun save(item: MediaItem, art: Bitmap?, sameTitle: Boolean): MediaItem {
        val artFile = File(dir, "${item.packageName}.$ART")
        when {
            art != null -> artFile.outputStream().use { art.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
            !sameTitle -> artFile.delete()
        }
        val p = Properties().apply {
            setProperty(TITLE, item.title)
            item.subtitle?.let { setProperty(SUBTITLE, it) }
            setProperty(UPDATED, item.updatedMs.toString())
        }
        File(dir, "${item.packageName}.$META").outputStream().use { p.store(it, null) }
        return item.copy(artPath = artFile.takeIf { it.exists() }?.absolutePath)
    }

    private companion object {
        const val DIR = "continue"
        const val META = "properties"
        const val ART = "jpg"
        const val TITLE = "title"
        const val SUBTITLE = "subtitle"
        const val UPDATED = "updated"
        const val JPEG_QUALITY = 85
    }
}
