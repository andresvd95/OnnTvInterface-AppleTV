package dev.onn.launcher.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Espejo de design/tokens.json (fuente unica). Base Android TV: 960x540 dp. Cambiar aqui solo si cambia ese archivo. */
object Tokens {
    object Colors {
        val background = Color(0xFF0D1424)
        val wallpaperA = Color(0xFF1B2A52)
        val wallpaperB = Color(0xFF2A1F4D)
        val surface = Color(0x17FFFFFF)        // rgba(255,255,255,0.09)
        val surfaceStrong = Color(0xD11E263C)  // rgba(30,38,60,0.82)
        val border = Color(0x29FFFFFF)         // rgba(255,255,255,0.16)
        val text = Color(0xFFF4F6FB)
        val textMuted = Color(0x9EF4F6FB)      // 0.62
        val primary = Color(0xFF2F5BD3)
        val focus = Color(0xFFFFFFFF)
        val onFocus = Color(0xFF0D1424)
        val danger = Color(0xFFFF6B5A)
    }
    object Type {
        val xs = 12.sp; val sm = 14.sp; val md = 16.sp; val lg = 20.sp; val xl = 28.sp; val x2l = 36.sp; val x3l = 48.sp
    }
    object Space {
        val s1 = 4.dp; val s2 = 8.dp; val s3 = 12.dp; val s4 = 16.dp; val s6 = 24.dp; val s8 = 32.dp
        val safeX = 48.dp; val safeY = 27.dp
    }
    object Radius { val sm = 8.dp; val md = 12.dp; val lg = 16.dp }
    object Size {
        val tileAppW = 160.dp; val tileAppH = 90.dp
        val tileRecentW = 120.dp; val tileRecentH = 68.dp
        val iconButton = 40.dp
        val focusRing = 2.dp
        const val focusScale = 1.1f
        val panelW = 220.dp
        val heroBannerW = 192.dp; val heroBannerH = 108.dp
    }
    object Motion { const val fastMs = 120; const val baseMs = 200; const val slowMs = 320; const val messageMs = 3_000L; const val carouselMs = 8_000L }
}