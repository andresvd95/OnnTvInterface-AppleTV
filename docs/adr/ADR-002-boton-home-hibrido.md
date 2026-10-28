# ADR-002 — Botón Home: estrategia híbrida decidida por la prueba de concepto

**Fecha:** 2026-10-26 · **Estado:** aceptado (variante final pendiente de la PoC) · **Decidió:** usuario (a propuesta del Arquitecto)

## Contexto
Perfiles (HU-012), notificaciones (HU-013) y quizá las recomendaciones dependen del launcher de Google TV (paquete por verificar, probablemente com.google.android.apps.tv.launcherx). Deshabilitarlo da el control total de Home, pero puede romperlos (riesgo 7).

## Opciones consideradas
| Criterio | A. Deshabilitar Google TV | B. Accesibilidad, Google TV activo | C. Híbrido por PoC |
|---|---|---|---|
| Control de Home | Total | Muy bueno (posible parpadeo) | El mejor posible |
| Perfiles/notificaciones | Riesgo alto | Funcionan | Protegidos |
| Reversibilidad | 1 comando | 1 comando | 1 comando |
| Complejidad | Baja | Media | Media |

## Decisión
Se elige **C. Híbrido**: por defecto se aplica **B** (servicio de accesibilidad que detecta que se abrió el launcher de Google TV y trae el nuestro al frente). La PoC mide el parpadeo y prueba **A** en la TV. Se pasa a A **solo si** el parpadeo es inaceptable para el usuario **y** A no rompe perfiles, notificaciones ni recomendaciones. La variante final se anota en este ADR.

## Consecuencias
- **Positivas:** no se rompe nada de Google TV por defecto; las dos vías se revierten con un comando.
- **Negativas / costos aceptados:** posible parpadeo breve al pulsar Home con B; el servicio de accesibilidad es un permiso potente (se acota en el modelo de amenazas del TDD).
- **Para revertirla:** apagar el servicio de accesibilidad y/o volver a habilitar el launcher de stock con el script restore.

---
## Resultado de la PoC (2026-10-26) → variante final: **B reforzada**
- Se queda Google TV **activo** (A descartada: perfiles y notificaciones viven en launcherx).
- El servicio de accesibilidad tiene tres capas: (1) **filtra la tecla Home** y trae nuestra pantalla antes de que Google TV se dibuje; (2) **detecta la ventana** de launcherx HomeActivity como respaldo; (3) **reintenta** hasta 3 veces si a los 500 ms nuestra pantalla no quedó arriba.
- El mismo servicio abre el **panel de notificaciones** con GLOBAL_ACTION_NOTIFICATIONS (el intent directo está prohibido para apps normales).
- Validado por el usuario con control físico: sin destello. Detalle: docs/06-poc-informe.md.
