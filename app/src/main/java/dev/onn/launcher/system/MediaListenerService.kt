package dev.onn.launcher.system

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.notification.NotificationListenerService

/**
 * HU-020: escucha las sesiones multimedia de las apps (título y portada de lo que se reproduce)
 * para la portada "Continuar". Android solo da ese acceso a un servicio de notificaciones habilitado;
 * el contenido de las notificaciones no se lee ni se guarda.
 */
class MediaListenerService : NotificationListenerService() {
    private val handler = Handler(Looper.getMainLooper())
    private val tracked = mutableMapOf<MediaSession.Token, Pair<MediaController, MediaController.Callback>>()
    private var manager: MediaSessionManager? = null
    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { track(it.orEmpty()) }

    override fun onListenerConnected() {
        MediaHub.load(this)
        val m = getSystemService(MediaSessionManager::class.java) ?: return
        manager = m
        val me = ComponentName(this, MediaListenerService::class.java)
        runCatching {
            m.addOnActiveSessionsChangedListener(sessionsListener, me, handler)
            track(m.getActiveSessions(me))
        }
    }

    override fun onListenerDisconnected() = release()

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    private fun release() {
        runCatching { manager?.removeOnActiveSessionsChangedListener(sessionsListener) }
        tracked.values.forEach { (c, cb) -> runCatching { c.unregisterCallback(cb) } }
        tracked.clear()
    }

    private fun track(controllers: List<MediaController>) {
        val live = controllers.map { it.sessionToken }.toSet()
        tracked.keys.filter { it !in live }.forEach { t ->
            tracked.remove(t)?.let { (c, cb) -> runCatching { c.unregisterCallback(cb) } }
        }
        controllers.filter { it.sessionToken !in tracked && it.packageName != packageName }.forEach { c ->
            val cb = object : MediaController.Callback() {
                override fun onMetadataChanged(metadata: MediaMetadata?) = capture(c.packageName, metadata, c.playbackState)
                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    if (state?.state == PlaybackState.STATE_PLAYING) capture(c.packageName, c.metadata, state)
                }
            }
            c.registerCallback(cb, handler)
            tracked[c.sessionToken] = c to cb
            capture(c.packageName, c.metadata, c.playbackState)
        }
    }

    private fun capture(pkg: String, md: MediaMetadata?, state: PlaybackState?) {
        if (md == null || state?.state == PlaybackState.STATE_ERROR) return
        val title = (md.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE) ?: md.getString(MediaMetadata.METADATA_KEY_TITLE))
            ?.takeIf { it.isNotBlank() } ?: return
        val subtitle = sequenceOf(
            MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, MediaMetadata.METADATA_KEY_ARTIST, MediaMetadata.METADATA_KEY_ALBUM,
        ).mapNotNull { md.getString(it)?.takeIf(String::isNotBlank) }.firstOrNull()
        MediaHub.record(this, pkg, title, subtitle, lastActivityMs(state)) { art(md) }
    }

    /** Hora real de la última actividad de la sesión, para no "rejuvenecer" sesiones viejas al reiniciar. */
    private fun lastActivityMs(state: PlaybackState?): Long {
        val now = System.currentTimeMillis()
        val t = state?.lastPositionUpdateTime ?: 0L
        if (t <= 0L || state?.state == PlaybackState.STATE_PLAYING) return now
        return now - (SystemClock.elapsedRealtime() - t).coerceAtLeast(0)
    }

    /** Portada: la imagen que trae la sesión o, si viene como dirección, solo si es local (sin Internet). */
    private fun art(md: MediaMetadata): Bitmap? {
        sequenceOf(MediaMetadata.METADATA_KEY_ART, MediaMetadata.METADATA_KEY_ALBUM_ART, MediaMetadata.METADATA_KEY_DISPLAY_ICON)
            .mapNotNull { md.getBitmap(it) }.firstOrNull()?.let { return it }
        md.description?.iconBitmap?.let { return it }
        val uri = sequenceOf(MediaMetadata.METADATA_KEY_ART_URI, MediaMetadata.METADATA_KEY_ALBUM_ART_URI, MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI)
            .mapNotNull { md.getString(it)?.let(Uri::parse) }.firstOrNull() ?: md.description?.iconUri ?: return null
        if (uri.scheme !in LOCAL_SCHEMES) return null
        return runCatching { contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull()
    }

    private companion object { val LOCAL_SCHEMES = setOf("content", "android.resource", "file") }
}
