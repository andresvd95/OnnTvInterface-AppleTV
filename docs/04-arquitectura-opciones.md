# Arquitectura — decisiones mayores (regla de 3 opciones)

**Rol:** architect · **Fecha:** 2026-10-26 · **PRD:** v0.2 aprobado

---

### Decisión 1 (ADR-001): stack de la app
**Contexto:** el launcher necesita APIs nativas de Android TV: lista de apps, proveedor de recomendaciones (TvContract), servicio de accesibilidad para el botón Home y abrir perfiles/notificaciones del sistema. Además debe ir fluido en el hardware modesto del onn. 4K Box y lograr la diagramación limpia tipo Apple con capas translúcidas.

| Criterio | A. Kotlin + Jetpack Compose for TV | B. Flutter (partiendo de FLauncher, código abierto) | C. Kotlin + Leanback (vistas clásicas) |
|---|---|---|---|
| Costo en tiempo | Medio | Bajo al inicio (ya hay base), medio después | Medio-alto |
| Acceso a APIs nativas (recomendaciones, accesibilidad, perfiles) | Directo | Por "puentes" a Kotlin: más código y más puntos de falla | Directo |
| Rendimiento en el onn. Box | Bueno; nativo | Bueno, pero el desenfoque tipo vidrio es costoso en Flutter | Muy bueno |
| Libertad para el diseño tipo Apple | Alta (UI declarativa moderna) | Alta | Baja: Leanback impone su propia diagramación |
| Mantenibilidad | Alta; es el camino oficial actual de Google para TV | Media; dependemos de decisiones de FLauncher | Baja; Leanback está en modo mantenimiento |
| Gasto de tokens de construcción | Medio | Medio (adaptar código ajeno + puentes) | Medio-alto |

**Recomendada: Opción A** — es el camino oficial de Google para TV, accede directo a todo lo que piden HU-010 a HU-013 y da libertad total de diseño.
**Qué perderías con la recomendada:** se arranca de cero, sin la base ya hecha de FLauncher.

---

### Decisión 2 (ADR-002): cómo se toma el botón Home
**Contexto:** los perfiles (HU-012), las notificaciones (HU-013) y posiblemente las recomendaciones dependen del launcher de Google TV (riesgo 7 de la viabilidad). Deshabilitarlo da el control más firme del botón Home, pero puede romper esas funciones.

| Criterio | A. Deshabilitar el launcher de Google TV | B. Mantenerlo activo y capturar Home con un servicio de accesibilidad | C. Híbrido decidido por la prueba de concepto |
|---|---|---|---|
| Control del botón Home | Total | Muy bueno; puede verse un parpadeo breve de Google TV | El mejor que permita la TV |
| Perfiles y notificaciones (HU-012/013) | **Riesgo alto de romperse** | Siguen funcionando | Se protegen: B por defecto |
| Reversibilidad | Un comando (`pm enable`) | Un comando (apagar el servicio) | Un comando en ambos casos |
| Resistencia a actualizaciones de Google TV | Media (pueden reactivarlo) | Media (pueden cambiar la accesibilidad) | Media |
| Complejidad | Baja | Media | Media (se prueban las dos en la PoC) |

**Recomendada: Opción C** — se arranca con B, que no rompe perfiles ni notificaciones, y la prueba de concepto en TV Santiago mide el parpadeo. Solo si el parpadeo es inaceptable y A no rompe nada, se pasa a A.
**Qué perderías con la recomendada:** un poco más de trabajo en la prueba de concepto, porque se prueban las dos vías.

---

### Nota sobre recomendaciones (HU-010), aplica a cualquier opción
Se elija lo que se elija, la prueba de concepto verifica si TV Santiago permite conceder por ADB el permiso de lectura de canales (`READ_EPG_DATA`) y qué recomendaciones de otras apps quedan visibles. Si es muy poco, se escala con opciones antes de construir.
