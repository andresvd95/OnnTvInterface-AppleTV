# Checklist de seguridad — v0.1.0

**Rol:** cybersecurity-engineer · **Fecha:** 2026-10-26 · **Base:** TDD §8 (STRIDE)

| # | Control | Evidencia | Estado |
|---|---|---|---|
| 1 | Permisos mínimos | `aapt dump permissions`: QUERY_ALL_PACKAGES, PACKAGE_USAGE_STATS y el permiso interno DYNAMIC_RECEIVER_NOT_EXPORTED (de AndroidX). **Sin INTERNET** | ✅ |
| 2 | Sin secretos ni URLs en el código | Búsqueda en app/src de URLs, claves y contraseñas: ninguna | ✅ |
| 3 | Servicio de accesibilidad acotado | Solo TYPE_WINDOW_STATE_CHANGED del paquete launcherx + tecla Home; `canRetrieveWindowContent=false`; no registra eventos | ✅ |
| 4 | Componentes exportados | HomeActivity (requerido para HOME); servicio protegido por BIND_ACCESSIBILITY_SERVICE; receptor de paquetes NOT_EXPORTED | ✅ |
| 5 | Datos guardados | Solo favoritas, ocultas (DataStore, sin datos personales); `allowBackup=false` | ✅ |
| 6 | Reversibilidad | Ningún uninstall/disable de sistema; restauración probada; rescate sin PC documentado | ✅ |
| 7 | Dependencias | Solo AndroidX/Compose oficiales de Google (BOM 2024.06.00, tv-material 1.0.0) | ✅ |
| 8 | Firma del APK | Build debug firmado con clave de depuración local (sin claves en el repo). Para uso doméstico es aceptable; una versión firmada propia queda como mejora | ⚠️ Aceptado |
| 9 | ADB abierto en la TV | Depuración inalámbrica activa mientras se desarrolla | ⚠️ Recomendado apagarla tras instalar |

**Dictamen:** apto para release v0.1.0 en la TV con los riesgos 8 y 9 aceptados.

## v1.1.0 — Portada "Continuar" (HU-020) · 2026-10-27
| # | Control | Evidencia | Estado |
|---|---|---|---|
| 10 | Servicio de notificaciones (`MediaListenerService`) | Protegido por BIND_NOTIFICATION_LISTENER_SERVICE. Solo usa `MediaSessionManager.getActiveSessions` para leer título, subtítulo e imagen de lo que se reproduce. **No** sobrescribe `onNotificationPosted`: no lee ni guarda notificaciones | ✅ |
| 11 | Datos guardados nuevos | `filesDir/continue/<paquete>.properties` (título, subtítulo, fecha) y `<paquete>.jpg` (imagen, máx. 1280 px). Solo en la TV, `allowBackup=false`, sin red. Se muestran como máximo 7 días | ✅ |
| 12 | Sin descargas | Imágenes con dirección web se ignoran; solo se leen las incluidas en la sesión o con esquema local (`content`, `android.resource`, `file`) | ✅ |
| 13 | Reversibilidad | Se habilita con `cmd notification allow_listener` (instalar/reaplicar) y se quita con `disallow_listener` en restaurar.ps1; desinstalar también lo elimina | ✅ |

**Dictamen:** apto para v1.1.0. El acceso a notificaciones es un permiso sensible: se documenta que la app no lee su contenido y el usuario puede negarlo; sin él, la portada muestra solo la app.
