# Especificación UX — Pantalla de inicio · v0.1 (borrador)

**Rol:** ux-ui-designer · **Fecha:** 2026-10-26 · **Historias:** HU-001…HU-013 · **Tokens:** design/tokens.json

## Propósito
Una sola pantalla, muy limpia: lo primero y más grande son las apps. Diagramación inspirada en el lenguaje actual de Apple (aire, jerarquía clara, superficies translúcidas, esquinas redondeadas). **Sin** íconos, tipografías ni marca de Apple: se usan las fuentes del sistema de Google TV y los banners de cada app.

## Estructura (de arriba abajo, orden fijo)
1. **Barra superior** — izquierda: hora (xl) y fecha (sm, text-muted). Derecha: cápsula de vidrio con 4 botones redondos: Notificaciones (con punto `danger` si hay pendientes), Google Play, Ajustes, Perfil (avatar).
2. **Apps** (HU-001, HU-002, HU-006) — fila horizontal de banners 16:9 (`tile-app`). Primero las favoritas en su orden; luego el resto alfabético. Foco inicial en la primera app.
3. **Recientes** (HU-010) — fila de banners pequeños sobre vidrio (`tile-recent`), máx. 10, de la más reciente a la más antigua.
4. Nada más. No hay recomendaciones, pósters ni anuncios en ninguna parte.

## Navegación con control
- ←/→ dentro de una fila; ↓/↑ entre barra superior, Apps y Recientes. El foco recuerda la última posición de cada fila.
- OK abre. **Mantener OK** en una app abre el menú de vidrio: Mover, Quitar de arriba / Fijar arriba, Ocultar (HU-006, HU-008).
- Atrás en la pantalla de inicio no hace nada (no sale del launcher).
- Foco: escala `focus-scale` + anillo `focus-ring` blanco + nombre de la app debajo (solo en la que tiene foco). Animación `base`; sin animación si "Quitar animaciones" está activo.

## Estados
| Estado | Qué se ve |
|---|---|
| **Éxito** | Mockup 1: barra superior + Apps + Recientes |
| **Carga** | Sin spinners: los banners aparecen con el color de fondo de cada app y la imagen entra con fundido `fast` (la lista de apps es local, < 300 ms) |
| **Vacío** | Sin Recientes: la fila no se muestra (mockup 3). Sin apps de terceros: texto "Instala apps desde Google Play" + botón Google Play con foco |
| **Error** | Si un acceso no abre (intent no resuelto): aviso de vidrio abajo, 3 s, "No se pudo abrir Ajustes. Intenta de nuevo." Sin códigos técnicos |

## Tamaños (TV)
| Tamaño | Comportamiento |
|---|---|
| 720p | Mismos dp; caben ~5 banners visibles; textos ≥ 16sp |
| 1080p (base) | ~6 banners visibles (mockup 1) |
| 4K | Android escala por densidad: misma composición, imágenes en alta resolución si la app las ofrece |

## Accesibilidad (WCAG 2.2 AA adaptado a TV)
- Texto `text` sobre `background` y sobre vidrio: contraste ≥ 7:1; `text-muted` ≥ 4.5:1 (se verifica en construcción con capturas).
- El foco nunca depende solo del color: escala + anillo + etiqueta.
- Todos los botones tienen descripción para TalkBack ("Notificaciones, 2 nuevas", "Ajustes", "Perfil de Andrés").
- Todo operable con D-pad + OK + Atrás + Home.

## Notas para construcción
- Vidrio sin desenfoque en vivo: fondo pre-desenfocado + `surface` (decisión de rendimiento, TDD §9).
- Ningún valor fuera de design/tokens.json.
