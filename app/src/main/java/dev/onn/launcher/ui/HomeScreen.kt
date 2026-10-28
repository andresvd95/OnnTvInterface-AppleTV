@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package dev.onn.launcher.ui

import android.os.SystemClock
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings as SettingsIcon
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TimerOff
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import dev.onn.launcher.R
import dev.onn.launcher.domain.AppEntry
import dev.onn.launcher.domain.HeroContent
import dev.onn.launcher.domain.LastUsed
import dev.onn.launcher.domain.SLEEP_OPTIONS_MIN
import dev.onn.launcher.domain.pickHero
import dev.onn.launcher.domain.remainingMinutes
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pantalla de inicio v0.4 (docs/ux/pantalla-inicio.md): portada a pantalla completa de la app con foco
 * (o de lo último reproducido), fila de Apps en el orden del usuario y una ventana pequeña de Recientes.
 * Panel lateral de vidrio que se oculta al entrar al contenido.
 */
@Composable
fun HomeScreen(vm: HomeViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val reducedMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    var panelVisible by remember { mutableStateOf(true) }
    var panelFocusRequests by remember { mutableIntStateOf(0) }
    var heroPkg by remember { mutableStateOf<String?>(null) }
    var menuFor by remember { mutableStateOf<MenuTarget?>(null) }
    var timerOpen by remember { mutableStateOf(false) }
    val homeItem = remember { FocusRequester() }
    val firstCard = remember { FocusRequester() }
    val cardsState = rememberLazyListState()

    // Portada (HU-020): la última app enfocada en las filas; sin foco en ellas, lo último reproducido.
    val focusedApp = heroPkg?.let { p -> (state.apps + state.recents).firstOrNull { it.app.packageName == p } }
    val hero = pickHero(focusedApp?.app, state.apps.map { it.app }, state.media)
    val heroCard = hero?.let { h -> state.apps.firstOrNull { it.app.packageName == h.app.packageName } }

    // Foco inicial en Inicio (spec v0.2) y cada vez que el panel vuelve.
    LaunchedEffect(state.loaded, panelFocusRequests) { if (state.loaded) homeItem.requestWhenReady() }

    fun showPanel() { panelVisible = true; heroPkg = null; panelFocusRequests++ }
    fun hidePanel() { panelVisible = false; runCatching { firstCard.requestFocus() } }
    // Al volver el panel, la fila regresa al inicio para que ninguna tarjeta quede debajo.
    LaunchedEffect(panelVisible) { if (panelVisible) cardsState.animateScrollToItem(0) }
    // Atrás alterna: con el panel visible lo cierra y pasa a las apps; si no, lo muestra.
    BackHandler(enabled = menuFor == null && !timerOpen && state.moving == null) {
        if (panelVisible) hidePanel() else showPanel()
    }
    // Al mover en la fila, mantener la app visible.
    LaunchedEffect(state.moving, state.apps) {
        val m = state.moving?.takeIf { !it.inPanel } ?: return@LaunchedEffect
        val i = state.apps.indexOfFirst { it.app.packageName == m.packageName }
        val visible = cardsState.layoutInfo.visibleItemsInfo.map { it.index }
        if (i >= 0 && (visible.size < 3 || i <= visible.first() || i >= visible.last())) {
            cardsState.animateScrollToItem((i - 1).coerceAtLeast(0))
        }
    }

    val contentStart by animateDpAsState(
        if (panelVisible) Tokens.Space.safeX / 2 + Tokens.Size.panelW + Tokens.Space.s6 else Tokens.Space.safeX,
        tween(if (reducedMotion) 0 else Tokens.Motion.baseMs), label = "contentStart",
    )
    val onContentFocus = { inside: Boolean -> if (inside) panelVisible = false }

    Box(Modifier.fillMaxSize().background(Tokens.Colors.background)) {
        val art = rememberHeroArt(hero, vm)
        HeroBackdrop(art, reducedMotion)

        Column(Modifier.fillMaxSize().padding(top = Tokens.Space.safeY, bottom = Tokens.Space.safeY)) {
            TopBar(state, vm, onTimer = { timerOpen = true }, modifier = Modifier.padding(start = contentStart, end = Tokens.Space.safeX))
            Spacer(Modifier.height(Tokens.Space.s6))
            HeroInfo(
                hero = hero, card = heroCard,
                modifier = Modifier.padding(start = contentStart),
                onOpen = { hero?.let { vm.launch(it.app) } ?: vm.open(Shortcut.PLAY, "Google Play") },
            )
            Spacer(Modifier.weight(1f))
            AppsRow(
                cards = state.apps,
                moving = state.moving?.takeIf { !it.inPanel }?.packageName,
                listState = cardsState,
                vm = vm,
                startPadding = contentStart,
                firstCard = firstCard,
                onFocusInside = onContentFocus,
                onCardFocused = { heroPkg = it },
                onLeftFromFirst = { showPanel() },
                onMenu = { menuFor = MenuTarget(it, MenuSource.ROW) },
            )
            if (state.recents.isNotEmpty()) {
                Spacer(Modifier.height(Tokens.Space.s2))
                RecentsWindow(
                    recents = state.recents, vm = vm,
                    modifier = Modifier.padding(start = contentStart),
                    onFocusInside = onContentFocus,
                    onCardFocused = { heroPkg = it },
                    onLeftFromFirst = { showPanel() },
                    onMenu = { menuFor = MenuTarget(it, MenuSource.RECENT) },
                )
            }
        }

        AnimatedVisibility(
            visible = panelVisible,
            enter = slideInHorizontally(tween(if (reducedMotion) 0 else Tokens.Motion.baseMs)) { -it } + fadeIn(),
            exit = slideOutHorizontally(tween(if (reducedMotion) 0 else Tokens.Motion.baseMs)) { -it } + fadeOut(),
        ) {
            SidePanel(
                state = state, vm = vm, homeItem = homeItem, firstCard = firstCard,
                onHome = { hidePanel() },
                onMenu = { menuFor = MenuTarget(it, MenuSource.PANEL) },
            )
        }

        state.message?.let { MessageToast(it, vm::clearMessage) }
        if (timerOpen) {
            TimerDialog(
                activeMinutesLeft = remainingMinutes(state.sleepEndAt, SystemClock.elapsedRealtime()),
                onPick = { timerOpen = false; vm.setSleepTimer(it) },
                onDismiss = { timerOpen = false },
            )
        }
        menuFor?.let { target ->
            AppMenu(
                target = target,
                pinned = target.app.packageName in state.favorites,
                onDismiss = { menuFor = null },
                onMove = { menuFor = null; vm.startMove(target.app.packageName, inPanel = target.source == MenuSource.PANEL) },
                onPin = { menuFor = null; vm.togglePin(target.app.packageName) },
                onHide = { menuFor = null; vm.hide(target.app.packageName) },
            )
        }
    }
}

private enum class MenuSource { ROW, PANEL, RECENT }
private data class MenuTarget(val app: AppEntry, val source: MenuSource)

/** Pide el foco y reintenta hasta que el elemento este dibujado. */
private suspend fun FocusRequester.requestWhenReady() {
    repeat(FOCUS_ATTEMPTS) {
        if (runCatching { requestFocus() }.isSuccess) return
        delay(FOCUS_RETRY_MS)
    }
}

@Composable
private fun rememberArt(packageName: String?, vm: HomeViewModel): AppArt? {
    val art by produceState<AppArt?>(null, packageName) { value = packageName?.let { vm.art(it) } }
    return art
}

/** Arte de la portada: la imagen del contenido si existe; si no, el banner de la app. */
@Composable
private fun rememberHeroArt(hero: HeroContent?, vm: HomeViewModel): AppArt? {
    val media = (hero as? HeroContent.Continue)?.media
    val art by produceState<AppArt?>(null, hero?.app?.packageName, media?.artPath, media?.updatedMs) {
        value = media?.let { vm.mediaArt(it) } ?: hero?.app?.packageName?.let { vm.art(it) }
    }
    return art
}

// ---------- Portada (HU-020) ----------

/** Fondo a pantalla completa: luz ambiente con el color del arte e imagen nítida a la derecha, fundida. */
@Composable
private fun HeroBackdrop(art: AppArt?, reducedMotion: Boolean) {
    val bg = Tokens.Colors.background
    Crossfade(art, animationSpec = tween(if (reducedMotion) 0 else Tokens.Motion.slowMs), label = "hero") { a ->
        Box(Modifier.fillMaxSize()) {
            if (a != null) {
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0.25f to Color.Transparent, 1f to a.ambient.copy(alpha = AMBIENT_ALPHA))))
                Box(Modifier.align(Alignment.TopEnd).fillMaxWidth(HERO_ART_W).fillMaxHeight(HERO_ART_H)) {
                    if (a.isBanner) {
                        Image(a.bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Image(a.bitmap, null, Modifier.align(Alignment.Center).size(HERO_ICON), contentScale = ContentScale.Fit)
                    }
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to bg, 0.45f to bg.copy(alpha = 0.35f), 0.7f to Color.Transparent)))
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to bg)))
                }
            }
            // Velos de legibilidad (contraste AA): arriba para la barra, abajo para las filas.
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to bg.copy(alpha = 0.75f), 0.2f to Color.Transparent)))
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.5f to Color.Transparent, 0.7f to bg.copy(alpha = 0.92f), 1f to bg)))
        }
    }
}

@Composable
private fun HeroInfo(hero: HeroContent?, card: CardItem?, modifier: Modifier, onOpen: () -> Unit) {
    Column(modifier.widthIn(max = HERO_TEXT_W)) {
        val eyebrow: String
        val title: String
        val subtitle: String?
        val action: String
        when (hero) {
            is HeroContent.Continue -> {
                eyebrow = stringResource(R.string.hero_continue_in, hero.app.label)
                title = hero.media.title
                subtitle = hero.media.subtitle
                action = stringResource(R.string.hero_continue)
            }
            is HeroContent.App -> {
                eyebrow = card?.let { lastUsedText(it.lastUsed) } ?: ""
                title = hero.app.label
                subtitle = null
                action = stringResource(R.string.hero_open)
            }
            null -> {
                eyebrow = ""
                title = stringResource(R.string.empty_apps)
                subtitle = null
                action = stringResource(R.string.action_play)
            }
        }
        Text(
            eyebrow.uppercase(), fontSize = Tokens.Type.xs, fontWeight = FontWeight.SemiBold,
            color = Tokens.Colors.textMuted, letterSpacing = EYEBROW_SPACING, maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(Tokens.Space.s2))
        Text(
            title,
            fontSize = if (hero is HeroContent.Continue) Tokens.Type.x2l else Tokens.Type.x3l,
            lineHeight = if (hero is HeroContent.Continue) 40.sp else 54.sp,
            fontWeight = FontWeight.Bold, color = Tokens.Colors.text,
            maxLines = if (hero is HeroContent.Continue) 2 else 1, overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(Tokens.Space.s1))
            Text(subtitle, fontSize = Tokens.Type.md, color = Tokens.Colors.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(Tokens.Space.s4))
        Surface(
            onClick = onOpen,
            shape = ClickableSurfaceDefaults.shape(CircleShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Tokens.Colors.text, contentColor = Tokens.Colors.onFocus,
                focusedContainerColor = Tokens.Colors.focus, focusedContentColor = Tokens.Colors.onFocus,
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = Tokens.Size.focusScale),
            border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(Tokens.Size.focusRing, Tokens.Colors.primary), shape = CircleShape)),
        ) {
            Row(
                Modifier.padding(start = Tokens.Space.s4, end = Tokens.Space.s6, top = Tokens.Space.s2, bottom = Tokens.Space.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(PANEL_ICON_GLYPH))
                Spacer(Modifier.width(Tokens.Space.s2))
                Text(action, fontSize = Tokens.Type.md, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun lastUsedText(l: LastUsed): String = when (l) {
    LastUsed.New -> stringResource(R.string.last_new)
    is LastUsed.Minutes -> stringResource(R.string.last_minutes, l.n.toInt())
    is LastUsed.Hours -> stringResource(R.string.last_hours, l.n.toInt())
    LastUsed.Yesterday -> stringResource(R.string.last_yesterday)
    is LastUsed.Days -> stringResource(R.string.last_days, l.n.toInt())
}

// ---------- Barra superior: accesos rapidos y reloj (HU-017) ----------

@Composable
private fun TopBar(state: HomeUiState, vm: HomeViewModel, onTimer: () -> Unit, modifier: Modifier) {
    val wifi = stringResource(R.string.quick_wifi)
    val bt = stringResource(R.string.quick_bluetooth)
    val audio = stringResource(R.string.quick_audio)
    val left = rememberRemainingMinutes(state.sleepEndAt)
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.background(Tokens.Colors.surface, CircleShape).border(1.dp, Tokens.Colors.border, CircleShape).padding(Tokens.Space.s1),
            horizontalArrangement = Arrangement.spacedBy(Tokens.Space.s1),
        ) {
            QuickChip(if (state.online) Icons.Outlined.Wifi else Icons.Outlined.WifiOff, if (state.online) wifi else stringResource(R.string.quick_no_network)) {
                vm.open(Shortcut.WIFI, wifi)
            }
            QuickChip(Icons.Outlined.Bluetooth, bt) { vm.open(Shortcut.BLUETOOTH, bt) }
            QuickChip(
                Icons.Outlined.Timer,
                if (left > 0) stringResource(R.string.quick_timer_left, left) else stringResource(R.string.quick_timer),
                active = left > 0, onClick = onTimer,
            )
            QuickChip(Icons.Outlined.Speaker, audio) { vm.open(Shortcut.AUDIO, audio) }
        }
        Spacer(Modifier.width(Tokens.Space.s6))
        BigClock()
    }
}

@Composable
private fun rememberRemainingMinutes(endAt: Long?): Int {
    val now by produceState(SystemClock.elapsedRealtime(), endAt) {
        while (endAt != null) {
            value = SystemClock.elapsedRealtime()
            delay(TIMER_TICK_MS)
        }
    }
    return remainingMinutes(endAt, now)
}

@Composable
private fun QuickChip(icon: ImageVector, label: String, active: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.semantics { contentDescription = label },
        shape = ClickableSurfaceDefaults.shape(CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (active) Tokens.Colors.primary else Color.Transparent,
            contentColor = Tokens.Colors.text,
            focusedContainerColor = Tokens.Colors.focus,
            focusedContentColor = Tokens.Colors.onFocus,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = Tokens.Size.focusScale),
    ) {
        Row(Modifier.padding(horizontal = Tokens.Space.s3, vertical = Tokens.Space.s2), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(PANEL_ICON_GLYPH))
            Spacer(Modifier.width(Tokens.Space.s2))
            Text(label, fontSize = Tokens.Type.xs, maxLines = 1)
        }
    }
}

@Composable
private fun BigClock() {
    val context = LocalContext.current
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000 - value % 60_000)
        }
    }
    val locale = Locale.getDefault()
    val time = DateFormat.getBestDateTimePattern(locale, if (DateFormat.is24HourFormat(context)) "Hm" else "hm").replace("a", "").trim()
    val date = DateFormat.getBestDateTimePattern(locale, "EEEEdMMM")
    Column(horizontalAlignment = Alignment.End) {
        Text(SimpleDateFormat(time, locale).format(Date(now)), fontSize = Tokens.Type.xl, fontWeight = FontWeight.Medium, color = Tokens.Colors.text)
        Text(SimpleDateFormat(date, locale).format(Date(now)), fontSize = Tokens.Type.xs, color = Tokens.Colors.textMuted)
    }
}

@Composable
private fun TimerDialog(activeMinutesLeft: Int, onPick: (Int?) -> Unit, onDismiss: () -> Unit) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestWhenReady() }
    Dialog(onDismissRequest = onDismiss) {
        BackHandler(onBack = onDismiss)
        val shape = RoundedCornerShape(Tokens.Radius.md)
        Column(
            Modifier.width(MENU_WIDTH).background(Tokens.Colors.surfaceStrong, shape).border(1.dp, Tokens.Colors.border, shape).padding(Tokens.Space.s2),
            verticalArrangement = Arrangement.spacedBy(Tokens.Space.s1),
        ) {
            Text(stringResource(R.string.timer_title), fontSize = Tokens.Type.md, fontWeight = FontWeight.Medium, color = Tokens.Colors.text, modifier = Modifier.padding(Tokens.Space.s2))
            MenuItem(Icons.Outlined.TimerOff, stringResource(R.string.timer_off), Modifier.focusRequester(first)) { onPick(null) }
            SLEEP_OPTIONS_MIN.forEach { m ->
                MenuItem(Icons.Outlined.Timer, stringResource(R.string.timer_option, m), Modifier) { onPick(m) }
            }
            if (activeMinutesLeft > 0) {
                Text(stringResource(R.string.quick_timer_left, activeMinutesLeft), fontSize = Tokens.Type.xs, color = Tokens.Colors.textMuted, modifier = Modifier.padding(Tokens.Space.s2))
            }
        }
    }
}

// ---------- Fila de Apps en el orden del usuario (HU-018) ----------

@Composable
private fun AppsRow(
    cards: List<CardItem>, moving: String?, listState: LazyListState, vm: HomeViewModel, startPadding: Dp, firstCard: FocusRequester,
    onFocusInside: (Boolean) -> Unit, onCardFocused: (String) -> Unit, onLeftFromFirst: () -> Unit,
    onMenu: (AppEntry) -> Unit,
) {
    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth().onFocusChanged { onFocusInside(it.hasFocus) },
        horizontalArrangement = Arrangement.spacedBy(Tokens.Space.s4),
        contentPadding = PaddingValues(start = startPadding, end = Tokens.Space.safeX, top = Tokens.Space.s3, bottom = Tokens.Space.s1),
    ) {
        itemsIndexed(cards, key = { _, c -> c.app.packageName }) { index, card ->
            val isMoving = moving == card.app.packageName
            var mod: Modifier = Modifier
            if (index == 0) mod = mod.focusRequester(firstCard)
            mod = mod.onPreviewKeyEvent { e ->
                when {
                    isMoving -> handleRowMoveKey(e, vm)
                    index == 0 && e.key == Key.DirectionLeft && e.type == KeyEventType.KeyDown -> { onLeftFromFirst(); true }
                    else -> false
                }
            }
            AppCard(
                card = card, vm = vm, modifier = mod, moving = isMoving,
                onFocused = { onCardFocused(card.app.packageName) },
                onClick = { vm.launch(card.app) }, onLongClick = { onMenu(card.app) },
            )
        }
    }
}

/** Modo Mover en la fila (HU-018): ← → reordenan; OK o Atrás terminan. */
private fun handleRowMoveKey(e: KeyEvent, vm: HomeViewModel): Boolean {
    val down = e.type == KeyEventType.KeyDown
    return when (e.key) {
        Key.DirectionLeft -> { if (down) vm.moveBy(-1); true }
        Key.DirectionRight -> { if (down) vm.moveBy(+1); true }
        Key.DirectionUp, Key.DirectionDown -> true
        Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.Back -> { if (!down) vm.endMove(); true }
        else -> false
    }
}

@Composable
private fun AppCard(
    card: CardItem, vm: HomeViewModel, modifier: Modifier, moving: Boolean,
    onFocused: () -> Unit, onClick: () -> Unit, onLongClick: () -> Unit,
) {
    val art = rememberArt(card.app.packageName, vm)
    val shape = RoundedCornerShape(Tokens.Radius.sm)
    val ring = if (moving) Tokens.Colors.primary else Tokens.Colors.focus
    Column(Modifier.width(Tokens.Size.tileAppW)) {
        Card(
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = modifier
                .size(Tokens.Size.tileAppW, Tokens.Size.tileAppH)
                .onFocusChanged { if (it.isFocused) onFocused() }
                .semantics { contentDescription = card.app.label },
            shape = CardDefaults.shape(shape),
            colors = CardDefaults.colors(containerColor = Tokens.Colors.surfaceStrong),
            scale = CardDefaults.scale(focusedScale = Tokens.Size.focusScale),
            border = CardDefaults.border(
                border = if (moving) Border(BorderStroke(Tokens.Size.focusRing, Tokens.Colors.primary), shape = shape) else Border.None,
                focusedBorder = Border(BorderStroke(Tokens.Size.focusRing * 2, ring), shape = shape),
            ),
        ) {
            ArtBox(art, card.app.label, Tokens.Size.tileAppH)
        }
        Spacer(Modifier.height(Tokens.Space.s3))
        if (moving) {
            Text(stringResource(R.string.menu_move_row_hint), fontSize = Tokens.Type.xs, color = Tokens.Colors.text, maxLines = 2)
        } else {
            Text(card.app.label, fontSize = Tokens.Type.sm, color = Tokens.Colors.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(lastUsedText(card.lastUsed), fontSize = Tokens.Type.xs, color = Tokens.Colors.textMuted, maxLines = 1)
        }
    }
}

@Composable
private fun ArtBox(art: AppArt?, label: String, height: Dp) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            art == null -> Text(label, fontSize = Tokens.Type.xs, color = Tokens.Colors.text, maxLines = 1)
            art.isBanner -> Image(art.bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else -> Image(art.bitmap, null, Modifier.size(height * 0.55f), contentScale = ContentScale.Fit)
        }
    }
}

// ---------- Ventana de Recientes, máximo 3, sin reordenar (HU-019) ----------

@Composable
private fun RecentsWindow(
    recents: List<CardItem>, vm: HomeViewModel, modifier: Modifier,
    onFocusInside: (Boolean) -> Unit, onCardFocused: (String) -> Unit, onLeftFromFirst: () -> Unit,
    onMenu: (AppEntry) -> Unit,
) {
    val shape = RoundedCornerShape(Tokens.Radius.md)
    Row(
        modifier
            .onFocusChanged { onFocusInside(it.hasFocus) }
            .background(Tokens.Colors.surfaceStrong, shape)
            .border(1.dp, Tokens.Colors.border, shape)
            .padding(horizontal = Tokens.Space.s3, vertical = Tokens.Space.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Tokens.Space.s3),
    ) {
        Text(stringResource(R.string.section_recents), fontSize = Tokens.Type.xs, fontWeight = FontWeight.SemiBold, color = Tokens.Colors.textMuted)
        recents.forEachIndexed { index, card ->
            val art = rememberArt(card.app.packageName, vm)
            val tileShape = RoundedCornerShape(Tokens.Radius.sm)
            var mod: Modifier = Modifier
            if (index == 0) {
                mod = mod.onPreviewKeyEvent { e ->
                    if (e.key == Key.DirectionLeft && e.type == KeyEventType.KeyDown) { onLeftFromFirst(); true } else false
                }
            }
            Card(
                onClick = { vm.launch(card.app) },
                onLongClick = { onMenu(card.app) },
                modifier = mod
                    .size(RECENT_W, RECENT_H)
                    .onFocusChanged { if (it.isFocused) onCardFocused(card.app.packageName) }
                    .semantics { contentDescription = card.app.label },
                shape = CardDefaults.shape(tileShape),
                colors = CardDefaults.colors(containerColor = Tokens.Colors.surface),
                scale = CardDefaults.scale(focusedScale = Tokens.Size.focusScale),
                border = CardDefaults.border(focusedBorder = Border(BorderStroke(Tokens.Size.focusRing, Tokens.Colors.focus), shape = tileShape)),
            ) {
                ArtBox(art, card.app.label, RECENT_H)
            }
        }
    }
}

// ---------- Panel lateral (HU-015) ----------

@Composable
private fun SidePanel(
    state: HomeUiState, vm: HomeViewModel, homeItem: FocusRequester, firstCard: FocusRequester,
    onHome: () -> Unit, onMenu: (AppEntry) -> Unit,
) {
    val shape = RoundedCornerShape(Tokens.Radius.lg)
    // -> desde cualquier item del panel va a la primera tarjeta (spec v0.2).
    val toCards = Modifier.focusProperties { right = firstCard }
    Column(
        Modifier
            .padding(start = Tokens.Space.safeX / 2, top = Tokens.Space.safeY, bottom = Tokens.Space.safeY)
            .width(Tokens.Size.panelW)
            .fillMaxHeight()
            .background(Tokens.Colors.surfaceStrong, shape)
            .border(1.dp, Tokens.Colors.border, shape)
            .padding(Tokens.Space.s2),
        verticalArrangement = Arrangement.spacedBy(Tokens.Space.s1),
    ) {
        val profile = stringResource(R.string.action_profile)
        PanelItem(Icons.Outlined.Person, profile, toCards, trailing = { ClockText() }, iconContainer = Tokens.Colors.primary) {
            vm.open(Shortcut.PROFILE, profile)
        }
        Spacer(Modifier.height(Tokens.Space.s1))
        if (state.canSearch) {
            val search = stringResource(R.string.nav_search)
            PanelItem(Icons.Outlined.Search, search, toCards) { vm.open(Shortcut.SEARCH, search) }
        }
        PanelItem(Icons.Outlined.Home, stringResource(R.string.nav_home), toCards.focusRequester(homeItem), selected = true, onClick = onHome)
        val notif = stringResource(R.string.action_notifications)
        PanelItem(Icons.Outlined.Notifications, notif, toCards) { vm.open(Shortcut.NOTIFICATIONS, notif) }
        val play = stringResource(R.string.action_play)
        PanelItem(Icons.Outlined.ShoppingCart, play, toCards) { vm.open(Shortcut.PLAY, play) }
        val settings = stringResource(R.string.action_settings)
        PanelItem(Icons.Outlined.SettingsIcon, settings, toCards) { vm.open(Shortcut.SETTINGS, settings) }

        if (state.pinned.isNotEmpty()) {
            Text(
                stringResource(R.string.section_my_apps), fontSize = Tokens.Type.xs, color = Tokens.Colors.textMuted,
                modifier = Modifier.padding(start = Tokens.Space.s2, top = Tokens.Space.s3, bottom = Tokens.Space.s1),
            )
            state.pinned.forEach { app ->
                val moving = state.moving?.let { it.inPanel && it.packageName == app.packageName } == true
                var mod = toCards
                if (moving) mod = mod.onPreviewKeyEvent { handleMoveKey(it, vm) }
                PinnedItem(app, vm, mod, moving, onClick = { vm.launch(app) }, onLongClick = { onMenu(app) })
            }
        }
    }
}

@Composable
private fun ClockText() {
    val context = LocalContext.current
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(60_000 - value % 60_000)
        }
    }
    val locale = Locale.getDefault()
    val pattern = DateFormat.getBestDateTimePattern(locale, if (DateFormat.is24HourFormat(context)) "Hm" else "hm").replace("a", "").trim()
    Text(SimpleDateFormat(pattern, locale).format(Date(now)), fontSize = Tokens.Type.xs, color = Tokens.Colors.textMuted)
}

@Composable
private fun PanelItem(
    icon: ImageVector, label: String, modifier: Modifier,
    selected: Boolean = false, iconContainer: Color? = null,
    trailing: (@Composable () -> Unit)? = null, onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(Tokens.Radius.md)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (selected) Tokens.Colors.surface else Color.Transparent,
            contentColor = Tokens.Colors.text,
            focusedContainerColor = Tokens.Colors.focus,
            focusedContentColor = Tokens.Colors.onFocus,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        Row(Modifier.padding(horizontal = Tokens.Space.s3, vertical = Tokens.Space.s2), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(PANEL_ICON).background(iconContainer ?: Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, contentDescription = null, modifier = Modifier.size(PANEL_ICON_GLYPH)) }
            Spacer(Modifier.width(Tokens.Space.s3))
            Text(label, fontSize = Tokens.Type.sm, modifier = Modifier.weight(1f), maxLines = 1)
            trailing?.invoke()
        }
    }
}

@Composable
private fun PinnedItem(app: AppEntry, vm: HomeViewModel, modifier: Modifier, moving: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val art = rememberArt(app.packageName, vm)
    Surface(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(Tokens.Radius.md)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (moving) Tokens.Colors.surface else Color.Transparent,
            contentColor = Tokens.Colors.text,
            focusedContainerColor = Tokens.Colors.focus,
            focusedContentColor = Tokens.Colors.onFocus,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        Row(Modifier.padding(horizontal = Tokens.Space.s3, vertical = Tokens.Space.s2), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(PANEL_ICON).clip(CircleShape).background(Tokens.Colors.surface)) {
                art?.let { Image(it.bitmap, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            }
            Spacer(Modifier.width(Tokens.Space.s3))
            Text(
                if (moving) stringResource(R.string.menu_move_hint) else app.label,
                fontSize = Tokens.Type.sm, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Modo Mover en "Mis apps" (HU-006): flechas arriba/abajo reordenan; OK o Atras terminan. */
private fun handleMoveKey(e: KeyEvent, vm: HomeViewModel): Boolean {
    val down = e.type == KeyEventType.KeyDown
    return when (e.key) {
        Key.DirectionUp -> { if (down) vm.moveBy(-1); true }
        Key.DirectionDown -> { if (down) vm.moveBy(+1); true }
        Key.DirectionCenter, Key.Enter, Key.NumPadEnter, Key.Back -> { if (!down) vm.endMove(); true }
        else -> false
    }
}

// ---------- Menu y avisos ----------

@Composable
private fun AppMenu(
    target: MenuTarget, pinned: Boolean,
    onDismiss: () -> Unit, onMove: () -> Unit, onPin: () -> Unit, onHide: () -> Unit,
) {
    val first = remember { FocusRequester() }
    LaunchedEffect(Unit) { first.requestWhenReady() }
    // Recientes no se reordenan (HU-019): sin "Mover".
    val canMove = target.source != MenuSource.RECENT
    Dialog(onDismissRequest = onDismiss) {
        BackHandler(onBack = onDismiss)
        val shape = RoundedCornerShape(Tokens.Radius.md)
        Column(
            Modifier.width(MENU_WIDTH).background(Tokens.Colors.surfaceStrong, shape).border(1.dp, Tokens.Colors.border, shape).padding(Tokens.Space.s2),
            verticalArrangement = Arrangement.spacedBy(Tokens.Space.s1),
        ) {
            Text(target.app.label, fontSize = Tokens.Type.md, fontWeight = FontWeight.Medium, color = Tokens.Colors.text, modifier = Modifier.padding(Tokens.Space.s2))
            if (canMove) MenuItem(Icons.Outlined.Edit, stringResource(R.string.menu_move), Modifier.focusRequester(first), onMove)
            MenuItem(
                Icons.Outlined.Star, stringResource(if (pinned) R.string.menu_unpin else R.string.menu_pin),
                if (canMove) Modifier else Modifier.focusRequester(first), onPin,
            )
            MenuItem(Icons.Outlined.Close, stringResource(R.string.menu_hide), Modifier, onHide)
        }
    }
}

@Composable
private fun MenuItem(icon: ImageVector, text: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(Tokens.Radius.sm)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent, contentColor = Tokens.Colors.text,
            focusedContainerColor = Tokens.Colors.focus, focusedContentColor = Tokens.Colors.onFocus,
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
    ) {
        Row(Modifier.padding(horizontal = Tokens.Space.s3, vertical = Tokens.Space.s2), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(PANEL_ICON_GLYPH))
            Spacer(Modifier.width(Tokens.Space.s3))
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
            modifier = Modifier.background(Tokens.Colors.surfaceStrong, CircleShape).border(1.dp, Tokens.Colors.border, CircleShape)
                .padding(horizontal = Tokens.Space.s4, vertical = Tokens.Space.s2),
        )
    }
}

private const val FOCUS_ATTEMPTS = 20
private const val FOCUS_RETRY_MS = 50L
private const val TIMER_TICK_MS = 30_000L
private const val HERO_ART_W = 0.64f
private const val HERO_ART_H = 0.7f
private const val AMBIENT_ALPHA = 0.45f
private val HERO_ICON = 160.dp
private val HERO_TEXT_W = 380.dp
private val EYEBROW_SPACING = 1.5.sp
private val RECENT_W = 96.dp
private val RECENT_H = 54.dp
private val PANEL_ICON = 24.dp
private val PANEL_ICON_GLYPH = 18.dp
private val MENU_WIDTH = 280.dp
