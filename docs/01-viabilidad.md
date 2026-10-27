# Dictamen de viabilidad — ONN LAUNCHER

**Rol:** feasibility-analyst · **Fecha:** 2026-10-26 · **Ruta:** PROYECTO

## Dictamen: ✅ VIABLE CON CONDICIONES

## Qué se evalúa
Construir una interfaz de inicio (launcher) propia para el onn. 4K Streaming Box "TV Santiago" (Google TV), instalada por ADB, sin publicarla en Play Store, como MVP rápido y con restauración garantizada del estado original.

## Argumentos a favor
- Android TV permite oficialmente que cualquier app actúe como pantalla de inicio: basta con declarar la categoría `HOME` (y `LEANBACK_LAUNCHER`) en la app. No hace falta root.
- Instalar una app propia por ADB (sideload) es una vía estándar de desarrollo. No toca la garantía ni el sistema.
- La comunidad deshabilita habitualmente el launcher de Google TV con `pm disable-user` para que el botón Home abra otro launcher. Es **reversible** con `pm enable`.
- El alcance es doméstico, sin cuentas, datos sensibles ni servidores, así que el costo y el riesgo son bajos.

## Riesgos principales
| # | Riesgo | Impacto | Probabilidad | Mitigación |
|---|---|---|---|---|
| 1 | Una actualización (OTA) de Google TV reactiva el launcher de stock o cambia el manejo del botón Home | Medio: vuelve a salir Google TV | Media | Script de "reaplicar" de un comando; documentado en el runbook |
| 2 | Deshabilitar el launcher de stock rompe alguna función ligada a él (perfiles, búsqueda por voz, recomendaciones, algún acceso a Ajustes) | Medio | Media | El MVP incluye acceso directo a Ajustes y a búsqueda; restauración probada **antes** de dejarlo fijo |
| 3 | Quedar sin interfaz usable si el launcher propio falla al arrancar | Alto | Baja | Nunca desinstalar paquetes de sistema; el launcher de stock solo se deshabilita; comando de rescate por ADB probado y documentado |
| 4 | Rendimiento: el hardware del onn. Box es modesto | Medio: interfaz lenta | Media | Especialista de Performance activo; presupuesto de arranque y fluidez en requisitos no funcionales |
| 5 | La depuración inalámbrica de Android 11+ requiere emparejamiento y puede apagarse al reiniciar | Bajo: incomodidad | Alta | Documentar el emparejamiento; el launcher no depende de ADB para funcionar |

## Supuestos (no verificados aún)
- TV Santiago corre Google TV sobre Android 12 o superior. Se verifica con ADB en el respaldo.
- El paquete del launcher de stock es `com.google.android.apps.tv.launcherx`. Se verifica con ADB.
- La familia usa la TV sobre todo para abrir apps de streaming.

## Alternativas consideradas
| Alternativa | Pros | Contras |
|---|---|---|
| **Usar un launcher existente** (Projectivy, Wolf) | Cero desarrollo, disponible hoy | No es "nuestra"; diseño y funciones limitadas a lo que ofrece |
| **Partir de uno de código abierto** (p. ej. FLauncher) | Arranque más rápido; base probada en Android TV | Hereda decisiones de otro; stack fijado por ese proyecto |
| **Construir desde cero** | Control total del diseño y de cada función | Más esfuerzo que las otras dos |

Recomendación del analista: construir propio, como pidió el usuario. La base y el stack los decide el Arquitecto con la regla de 3 opciones, y una de ellas puede ser partir de código abierto.

## Condiciones medibles (obligatorias antes de Deploy)
1. **Respaldo verificado:** lista completa de paquetes (incluidos los deshabilitados), launcher por defecto actual, versión de Android/build y ajustes clave, guardados en `backups/tv-santiago/AAAA-MM-DD/`.
2. **Restauración probada:** el script de restauración devuelve TV Santiago al launcher de Google TV, comprobado en el dispositivo **antes** de dejar fijo el launcher propio.
3. **Solo cambios reversibles:** ningún `uninstall` de paquetes del sistema; solo `install` de nuestra app y `disable-user` / `enable` del launcher de stock.
4. **Rescate documentado:** comando de un solo paso en el runbook para volver al estado original desde el PC.

## Estimación (rango, no certeza)
MVP en pocas sesiones de trabajo. Depende del alcance que salga de Requerimientos y del stack elegido.

---

## Anexo A — Reevaluación por cambio de alcance (PRD v0.2) · 2026-10-26

**Cambio:** entran recomendaciones de las apps instaladas (debajo de la fila de apps), acceso a Google Play, perfiles de Google TV y notificaciones del sistema.

**Dictamen se mantiene: ✅ VIABLE CON CONDICIONES**, con un riesgo nuevo de impacto alto:

| # | Riesgo | Impacto | Probabilidad | Mitigación / qué debe resolver el Arquitecto |
|---|---|---|---|---|
| 6 | **Recomendaciones:** Android TV guarda las recomendaciones de cada app en su proveedor de canales; leer las de *otras* apps exige un permiso reservado al sistema. Un launcher instalado por ADB puede no tener acceso. | Alto: HU-010 podría no cumplirse tal cual | Media-alta | Verificar en TV Santiago qué permisos se pueden conceder por ADB y qué expone el sistema. Alternativas a evaluar: recomendaciones parciales (solo las que el sistema deje leer), fila "Seguir viendo" del sistema, o mostrar la vista de la propia app |
| 7 | **Perfiles y notificaciones viven dentro del launcher de Google TV.** Si se deshabilita (plan original para el botón Home), el selector de perfiles y el panel de notificaciones pueden dejar de funcionar | Alto: rompe HU-012 / HU-013 | Alta | El Arquitecto compara: (a) deshabilitar el launcher de stock, (b) **no** deshabilitarlo y capturar el botón Home con un servicio de accesibilidad (técnica usada por launchers como Projectivy), (c) híbrido. La opción elegida queda en ADR-001 |

**Condición nueva (antes de Construcción):**
5. **Prueba de concepto en TV Santiago** que confirme: qué recomendaciones se pueden leer, cómo se abren perfiles y notificaciones, y cómo se toma el botón Home sin romperlos. Si una historia Must resulta imposible, se escala al usuario con 2–3 opciones antes de construir.
