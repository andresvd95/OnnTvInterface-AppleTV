# Informe de la prueba de concepto (condición 5 de viabilidad)

**Fecha:** 2026-10-26 · **Dispositivo:** TV Santiago (onn. 4K, Android 14 / SDK 34) · **App:** poc/ (desechable, dev.onn.poc)

| # | Pregunta | Resultado | Evidencia |
|---|---|---|---|
| 1 | Versión y launcher de stock | Android 14 (SDK 34); HOME = com.google.android.apps.tv.launcherx/.home.HomeActivity | backups/tv-santiago/2026-10-26 |
| 2 | Recomendaciones de otras apps (HU-010) | ❌ **No accesibles.** READ_EPG_DATA es normal y se concede, pero canales, programas de vista previa y "Seguir viendo" devuelven 0 filas: leer los de otras apps exige ACCESS_ALL_EPG_DATA (signature, solo sistema) | log ONNPOC: channels {}, preview_programs {}, watch_next {} |
| 3a | Perfiles (HU-012) | ✅ Se abre el selector "Elige una cuenta" de Google TV con un intent directo a launcherx ProfileChooserActivity | captura profiles.png |
| 3b | Notificaciones (HU-013) | ⚠️ El intent estándar OPEN_NOTIFICATION_HANDLER_PANEL está **prohibido** (requiere STATUS_BAR_SERVICE). ✅ Funciona con el servicio de accesibilidad: performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS) abre el panel de Google TV | captura notif-a11y.png |
| 3c | Ajustes (HU-004) y Google Play (HU-011) | ✅ Ambos abren | log ONNPOC |
| 4 | Botón Home por accesibilidad (ADR-002 B) | ✅ Captura todas las pulsaciones. Del aviso del sistema a nuestra pantalla visible: 28–217 ms (típico 35–170). A veces llegan 2 avisos seguidos: hay que ignorar duplicados | log ONNPOC: INTERCEPT #1–#9 |
| 5 | Imágenes de recomendaciones | No aplica (no hay acceso) → **no se necesita permiso de Internet** | — |
| — | Apps lanzables (HU-001) | ✅ 15 apps Leanback detectadas | log ONNPOC |

## Conclusiones
- **ADR-002:** se confirma la variante **B (accesibilidad, Google TV activo)**. La variante A (deshabilitar Google TV) rompería perfiles y notificaciones, que dependen de launcherx, así que se descarta salvo que el usuario rechace el parpadeo.
- **HU-010 (recomendaciones) no es viable tal como está escrita.** Se escala al usuario con opciones.
- **Notificaciones** depende del servicio de accesibilidad: mismo componente que el botón Home; no agrega permisos.
- **Hallazgo de build:** la ruta del usuario tiene "é"; Gradle/Java en Windows requiere `jdk.net.unixdomain.tmpdir` en ruta ASCII y `android.overridePathCheck=true`. Para el proyecto real se recomienda una ruta sin tildes o mantener estos ajustes.

## Pendiente
- Juicio visual del usuario sobre el parpadeo al pulsar Home.
- Prueba de restauración (condición 2): quitar accesibilidad, desinstalar la PoC y verificar que HOME vuelve a launcherx.

## Ajuste tras la prueba del usuario (2026-10-26)
- El usuario pulsó Home muchas veces seguidas (~cada 0,2 s): la **última** pulsación dejó Google TV arriba (la pantalla de stock se reabrió justo después de nuestra llamada).
- **Corrección probada:** reintento: si 500 ms después de interceptar nuestra pantalla no volvió a primer plano, se trae de nuevo (máx. 3). Prueba de estrés: YouTube abierto + 10 pulsaciones de Home cada 0,2 s, 2 rondas → **ambas terminan en nuestra pantalla** (30 y 36 intercepciones). Este comportamiento pasa a ser requisito del HomeInterceptor real.
- **HU-010:** el usuario elige **fila de Recientes** (apps usadas recientemente) en lugar de recomendaciones de contenido.

## Captura de la tecla Home (sin destello) · 2026-10-26
- Con solo la detección de ventana, el usuario veía Google TV en ~50% de las pulsaciones (< 1 s).
- Se agregó **filtrado de teclas** al servicio de accesibilidad (`flagRequestFilterKeyEvents`, `onKeyEvent` con KEYCODE_HOME): nuestra pantalla se lanza al soltar Home, antes de que Google TV se dibuje.
- Prueba del usuario con control físico desde YouTube, Disney y Paramount: **ya no se ve Google TV**. Registro: 15 teclas Home consumidas, 0 reintentos.
- Nota: el sistema igual registra la ventana de Google TV (15 eventos); nuestra pantalla llega primero. Se mantienen las tres capas: tecla Home + detección de ventana + reintento.

## Restauración (condición 2) · ✅ probada
Comandos: `settings delete secure enabled_accessibility_services` · `settings put secure accessibility_enabled 0` · `uninstall dev.onn.poc`.
Resultado (`backups/tv-santiago/2026-10-26/verificacion-restauracion.txt`): HOME = launcherx, accesibilidad 0/null, deshabilitados = solo com.netflix.ninja, **0 diferencias de paquetes** frente al respaldo.

## Estado de condiciones de viabilidad
| # | Condición | Estado |
|---|---|---|
| 1 | Respaldo verificado | ✅ |
| 2 | Restauración probada | ✅ |
| 3 | Solo cambios reversibles | ✅ (nada desinstalado ni deshabilitado del sistema) |
| 4 | Rescate documentado | ⏳ pasa al runbook en Documentación (los 3 comandos de arriba) |
| 5 | Prueba de concepto | ✅ |
