# ADR-001 — Kotlin + Jetpack Compose for TV como stack del launcher

**Fecha:** 2026-10-26 · **Estado:** aceptado · **Decidió:** usuario (a propuesta del Arquitecto)

## Contexto
El launcher necesita APIs nativas de Android TV (apps instaladas, recomendaciones vía TvContract, servicio de accesibilidad, abrir perfiles/notificaciones del sistema). Además debe ir fluido en el onn. 4K Box y permitir una diagramación limpia estilo Apple con capas translúcidas.

## Opciones consideradas
| Criterio | A. Kotlin + Compose for TV | B. Flutter (base FLauncher) | C. Kotlin + Leanback |
|---|---|---|---|
| Costo en tiempo | Medio | Bajo al inicio, medio después | Medio-alto |
| APIs nativas | Directo | Por puentes | Directo |
| Rendimiento | Bueno | Bueno; vidrio costoso | Muy bueno |
| Libertad de diseño | Alta | Alta | Baja |
| Mantenibilidad | Alta | Media | Baja |

## Decisión
Se elige **A. Kotlin + Jetpack Compose for TV** porque es el camino oficial de Google para TV, accede directo a todo lo que piden HU-010 a HU-013 y da libertad total de diseño.

## Consecuencias
- **Positivas:** un solo lenguaje, sin puentes; librerías oficiales de foco y navegación con D-pad; Baseline Profiles para rendimiento.
- **Negativas / costos aceptados:** se parte de cero; hay que instalar en el PC el kit de Android (JDK 17, Android SDK, Gradle).
- **Para revertirla:** reescribir la capa de UI; las capas de datos y sistema en Kotlin serían reutilizables.
