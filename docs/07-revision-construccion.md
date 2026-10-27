# Revisión de Construcción — MVP v0.1.0

**Rol:** code-reviewer + qa-engineer · **Fecha:** 2026-10-26 · **Rama:** feature/mvp-launcher

## Resultado: ✅ APROBADA con observaciones (no bloqueantes)

## Pruebas (QA, escritas desde las historias antes de implementar)
| Suite | Historias | Resultado |
|---|---|---|
| HomeOrderingTest | HU-001, HU-006, HU-008 | 8/8 ✅ |
| RecentsPolicyTest | HU-010 | 5/5 ✅ |
| HomeKeyGateTest | HU-003 (ráfagas y reintento de la PoC) | 4/4 ✅ |
| **Total** | | **17/17 ✅** |

Pendiente para QA en dispositivo (no se puede probar en JVM): HU-002 navegación y foco, HU-003 Home real, HU-004/011/012/013 accesos, HU-005 instalación y restauración, estados de la spec UX, rendimiento (< 1,5 s).

## Revisión de código
| # | Hallazgo | Severidad | Estado |
|---|---|---|---|
| 1 | El ViewModel leía las estadísticas de uso en el hilo principal al crearse | Media | ✅ Corregido: arranca vacío y se llena en onResume en hilo de E/S |
| 2 | Tokens.kt es un espejo manual de design/tokens.json (la regla pide exportación generada) | Baja | Deuda técnica: generar desde el JSON en una iteración siguiente |
| 3 | "Quitar animaciones" del sistema no se respeta explícitamente en la escala de foco | Baja | Pendiente (spec UX, accesibilidad) |
| 4 | Punto de notificaciones pendientes en la campana no implementado (requiere permiso de escucha de notificaciones) | Baja | Fuera del MVP; la spec lo marcaba como supuesto |
| 5 | Sin permiso de Internet; servicio de accesibilidad con configuración mínima y protegido por BIND_ACCESSIBILITY_SERVICE | — | ✅ Conforme a STRIDE (TDD §8) |
| 6 | Ningún color ni medida literal en la UI fuera de Tokens (salvo tamaños de bitmap y alto de etiqueta, declarados como constantes) | — | ✅ |

## Notas de build (Windows, usuario con "é")
Compilar: unidad virtual ASCII para el proyecto y para la caché de Gradle (`subst`), `GRADLE_USER_HOME` en esa unidad y `jdk.net.unixdomain.tmpdir` en `C:\Users\Public\onn-tmp`. Sin esto el ejecutor de pruebas de Gradle no arranca.