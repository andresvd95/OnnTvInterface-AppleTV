@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package dev.onn.launcher.ui

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.onn.launcher.R
import dev.onn.launcher.data.AppRepository
import dev.onn.launcher.domain.AppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Pantalla de inicio (docs/ux/pantalla-inicio.md): barra superior, Apps, Recientes. Nada mas. */
@Composable
fun HomeScreen(vm: HomeViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    var menuFor by remember { mutableStateOf<AppEntry?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Tokens.Colors.background)
            .drawBehind {
                drawCircle(Tokens.Colors.wallpaperA, size.width * 0.45f, Offset(size.width * 0.1f, 0f), alpha = 0.7f)
                drawCircle(Tokens.Colors.wallpaperB, size.width * 0.35f, Offset(size.width * 0.95f, size.height), alpha = 0.6f)
            },
    ) {
        Column(Modifier.fillMaxSize().padding(vertical = Tokens.Space.safeY)) {
            TopBar(vm)
            Spacer(Modifier.height(Tokens.Space.s6))
            if (state.loaded && state.apps.isEmpty()) {
                EmptyApps(onPlay = { vm.open(Shortcut.PLAY, "Google Play") })
            } else {
                SectionLabel(stringResource(R.string.section_apps))
                AppsRow(state, vm, onMenu = { menuFor = it })
            }
            if (state.recents.isNotEmpty()) {
                Spacer(Modifier.height(Tokens.Space.s3))
                SectionLabel(stringResource(R.string.section_recents))
                RecentsRow(state.recents, vm)
            }
        }
        state.message?.let { MessageToast(it, vm::clearMessage) }
        menuFor?.let { app ->
            AppMenu(
                app = app,
                pinned = app.packageName in state.favorites,
                onDismiss = { menuFor = null },
                onMove = { menuFor = null; vm.startMove(app.packageName) },
                onPin = { menuFor = null; vm.togglePin(app.packageName) },
                onHide = { menuFor = null; vm.hide(app.packageName) },
            )
        }
    }
}

@Composable
private fun TopBar(vm: HomeViewModel) {
    Row(Modifier.padding(horizontal = Tokens.Space.safeX), verticalAlignment = Alignment.Top) {
        Clock(Modifier.weight(1f))
        Row(
            Modifier
                .background(Tokens.Colors.surface, CircleShape)
                .border(1.dp, Tokens.Colors.border, CircleShape)
                .padding(Tokens.Space.s1),
            horizontalArrangement = Arrangement.spacedBy(Tokens.Space.s1),
        ) {
            val notif = stringResource(R.string.action_notifications)
            val play = stringResource(R.string.action_play)
            val settings = stringResource(R.string.action_settings)
            val profile = stringResource(R.string.action_profile)
            RoundButton(Icons.Outlined.Notifications, notif) { vm.open(Shortcut.NOTIFICATIONS, notif) }
            RoundButton(Icons.Outlined.ShoppingCart, play) { vm.open(Shortcut.PLAY, play) }
            RoundButton(Icons.Outlined.Settings, settings) { vm.open(Shortcut.SETTINGS, settings) }
            RoundButton(Icons.Outlined.Person, profile, container = Tokens.Colors.primary) { vm.open(Shortcut.PROFILE, profile) }
        }
    }
}

@Composable
private fun Clock(modifier: Modifier) {
    val context = LocalContext.current
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000 - value % 60_000)
        }
    }
    val locale = Locale.getDefault()
    // Sin a. m./p. m., como en el mockup.
    val time = DateFormat.getBestDateTimePattern(locale, if (DateFormat.is24HourFormat(context)) "Hm" else "hm").replace("a", "").trim()
    val date = DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM")
    Column(modifier) {
        Text(SimpleDateFormat(time, locale).format(Date(now)), fontSize = Tokens.Type.x3l, fontWeight = FontWeight.Medium, color = Tokens.Colors.text)
        Text(SimpleDateFormat(date, locale).format(Date(now)), fontSize = Tokens.Type.md, color = Tokens.Colors.textMuted)
    }
}

@Composable
private fun RoundButton(icon: ImageVector, label: String, container: Color = Color.Transparent, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(Tokens.Size.iconButton).semantics { contentDescription = label },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = container,
            contentColor = Tokens.Colors.text,
            focusedContainerColor = Tokens.Colors.focus,
            focusedContentColor = Tokens.Colors.onFocus,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = Tokens.Size.focusScale),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = Tokens.Type.md, color = Tokens.Colors.textMuted, modifier = Modifier.padding(start = Tokens.Space.safeX))
}

@Composable
private fun AppsRow(state: HomeUiState, vm: HomeViewModel, onMenu: (AppEntry) -> Unit) {
    val first = remember { FocusRequester() }
    val hasApps = state.apps.isNotEmpty()
    // HU-001 criterio 3: foco inicial en la primera app. Se reintenta hasta que la fila este dibujada.
    LaunchedEffect(hasApps) {
        if (!hasApps) return@LaunchedEffect
        repeat(FOCUS_ATTEMPTS) {
            if (runCatching { first.requestFocus() }.isSuccess) return@LaunchedEffect
            delay(FOCUS_RETRY_MS)
        }
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Tokens.Space.s3),
        contentPadding = PaddingValues(horizontal = Tokens.Space.safeX, vertical = Tokens.Space.s3),
    ) {
        itemsIndexed(state.apps, key = { _, app -> app.packageName }) { index, app ->
            val moving = state.moving == app.packageName
            var mod: Modifier = if (index == 0) Modifier.focusRequester(first) else Modifier
            if (moving) mod = mod.onPreviewKeyEvent { handleMoveKey(it, vm) }
            AppTile(
                app = app, repo = vm.repo,
                width = Tokens.Size.tileAppW, height = Tokens.Size.tileAppH,
                glass = false, moving = moving, modifier = mod,
                onClick = { vm.launch(app) }, onLongClick = { onMenu(app) },
            )
        }
    }
}

/** Modo Mover (HU-006): flechas reordenan, OK o Atras terminan. Consume la tecla para no abrir la app. */
private fun handleMoveKey(e: KeyEvent, vm: HomeViewModel): Boolean {
    val down = e.type == KeyEventType.KeyDown
    return when (e.key) {
        Key.DirectionLeft -> { if (down) vm.moveBy(-1); true }
        Key.DirectionRight -> { if (down) vm.moveBy(+1); true }
        Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.Back -> { if (!down) vm.endMove(); true }
        else -> false
    }
}

@Composable
private fun RecentsRow(recents: List<AppEntry>, vm: HomeViewModel) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Tokens.Space.s2),
        contentPadding = PaddingValues(horizontal = Tokens.Space.safeX, vertical = Tokens.Space.s2),
    ) {
        itemsIndexed(recents, key = { _, app -> app.packageName }) { _, app ->
            AppTile(
                app = app, repo = vm.repo,
                width = Tokens.Size.tileRecentW, height = Tokens.Size.tileRecentH,
                glass = true, moving = false, modifier = Modifier,
                onClick = { vm.launch(app) }, onLongClick = null,
            )
        }
    }
}

@Composable
private fun AppTile(
    app: AppEntry, repo: AppRepository, width: Dp, height: Dp, glass: Boolean, moving: Boolean,
    modifier: Modifier, onClick: () -> Unit, onLongClick: (() -> Unit)?,
) {
    var focused by remember { mutableStateOf(false) }
    val visual by produceState<Pair<ImageBitmap, Boolean>?>(null, app.packageName) {
        value = withContext(Dispatchers.IO) {
            repo.visual(app.packageName)?.let { v ->
                val bmp = if (v.isBanner) v.drawable.toBitmap(BANNER_PX_W, BANNER_PX_H) else v.drawable.toBitmap(ICON_PX, ICON_PX)
                bmp.asImageBitmap() to v.isBanner
            }
        }
    }
    val shape = RoundedCornerShape(Tokens.Radius.sm)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = modifier
                .size(width, height)
                .onFocusChanged { focused = it.isFocused }
                .semantics { contentDescription = app.label },
            shape = CardDefaults.shape(shape),
            colors = CardDefaults.colors(containerColor = if (glass) Tokens.Colors.surface else Tokens.Colors.surfaceStrong),
            scale = CardDefaults.scale(focusedScale = Tokens.Size.focusScale),
            border = CardDefaults.border(
                border = if (glass) Border(BorderStroke(1.dp, Tokens.Colors.border), shape = shape) else Border.None,
                focusedBorder = Border(BorderStroke(Tokens.Size.focusRing, Tokens.Colors.focus), shape = shape),
            ),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val v = visual
                when {
                    v == null -> Text(app.label, fontSize = Tokens.Type.sm, color = Tokens.Colors.text, maxLines = 1)
                    v.second -> Image(v.first, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else -> Image(v.first, null, Modifier.size(height * 0.55f), contentScale = ContentScale.Fit)
                }
            }
        }
        Spacer(Modifier.height(Tokens.Space.s1))
        Box(Modifier.width(width).height(LABEL_HEIGHT), contentAlignment = Alignment.TopCenter) {
            val label = when {
                moving -> stringResource(R.string.menu_move_hint)
                focused -> app.label
                else -> ""
            }
            Text(label, fontSize = Tokens.Type.sm, color = Tokens.Colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmptyApps(onPlay: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    Column(Modifier.padding(horizontal = Tokens.Space.safeX)) {
        Text(stringResource(R.string.empty_apps), fontSize = Tokens.Type.lg, color = Tokens.Colors.text)
        Spacer(Modifier.height(Tokens.Space.s3))
        MenuItem(Icons.Outlined.ShoppingCart, stringResource(R.string.action_play), Modifier.focusRequester(focus), onPlay)
    }
}

@Composable
private fun AppMenu(
    app: AppEntry, pinned: Boolean,
    onDismiss: () -> Unit, onMove: () -> Unit, onPin: () -> Unit, onHide: () -> Unit,
) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { first.requestFocus() } }
    Dialog(onDismissRequest = onDismiss) {
        BackHandler(onBack = onDismiss)
        val shape = RoundedCornerShape(Tokens.Radius.md)
        Column(
            Modifier
                .width(MENU_WIDTH)
                .background(Tokens.Colors.surfaceStrong, shape)
                .border(1.dp, Tokens.Colors.border, shape)
                .padding(Tokens.Space.s2),
            verticalArrangement = Arrangement.spacedBy(Tokens.Space.s1),
        ) {
            Text(app.label, fontSize = Tokens.Type.md, fontWeight = FontWeight.Medium, color = Tokens.Colors.text,
                modifier = Modifier.padding(Tokens.Space.s1))
            MenuItem(Icons.Outlined.Edit, stringResource(R.string.menu_move), Modifier.focusRequester(first), onMove)
            MenuItem(Icons.Outlined.Star, stringResource(if (pinned) R.string.menu_unpin else R.string.menu_pin), Modifier, onPin)
            MenuItem(Icons.Outlined.Close, stringResource(R.string.menu_hide), Modifier, onHide)
        }
    }
}

@Composable
private fun MenuItem(icon: ImageVector, text: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(Tokens.Radius.sm)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            contentColor = Tokens.Colors.text,
            focusedContainerColor = Tokens.Colors.focus,
            focusedContentColor = Tokens.Colors.onFocus,
        ),
    ) {
        Row(Modifier.padding(horizontal = Tokens.Space.s2, vertical = Tokens.Space.s1 + 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(Tokens.Space.s2))
            Text(text, fontSize = Tokens.Type.md)
        }
    }
}

@Composable
private fun MessageToast(text: String, onDone: () -> Unit) {
    LaunchedEffect(text) { delay(Tokens.Motion.messageMs); onDone() }
    Box(Modifier.fillMaxSize().padding(bottom = Tokens.Space.s6), contentAlignment = Alignment.BottomCenter) {
        Text(
            text, fontSize = Tokens.Type.md, color = Tokens.Colors.text,
            modifier = Modifier
                .background(Tokens.Colors.surfaceStrong, CircleShape)
                .border(1.dp, Tokens.Colors.border, CircleShape)
                .padding(horizontal = Tokens.Space.s4, vertical = Tokens.Space.s2),
        )
    }
}

private const val BANNER_PX_W = 320
private const val BANNER_PX_H = 180
private const val ICON_PX = 96
private const val FOCUS_ATTEMPTS = 20
private const val FOCUS_RETRY_MS = 50L
private val LABEL_HEIGHT = 20.dp
private val MENU_WIDTH = 280.dp