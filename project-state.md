# project-state.md — ONN LAUNCHER

> Memoria viva del proyecto (formato Engram, ver `protocols/memory-engram.md`). **Solo el Orquestador escribe aquí.** Sin secretos. Objetivo: < 200 líneas.

**Última actualización:** 2026-10-26 · **Pack activo:** ninguno (núcleo vedev_agents v0.2.0, copia local en `1 PROYECTOS/VeDev_Agents`)

## 1. Identidad
- **Qué es:** Interfaz (launcher / pantalla de inicio) propia para Android TV, diseñada a medida para los onn. 4K Streaming Box de la casa.
- **Situación:** nuevo (greenfield)
- **Tipo:** otro — app Android TV (launcher)
- **Dispositivo piloto:** TV Santiago — onn. 4K Streaming Box — 192.168.1.8 (WiFi, Cast 3.72.446070)
- **Alcance de dispositivos:** solo TV Santiago. TV VD (192.168.1.17) queda fuera del proyecto por decisión del usuario.

## 2. Intake
- Dependencias: independiente; corre sobre Google TV / Android TV del onn. Box. Lee las apps instaladas del sistema.
- API: no expone. Consumo de APIs de terceros: ninguno en el MVP (supuesto del Orquestador).
- Equipo: 1 persona (dueño/usuario) + equipo vedev_agents.
- Datos sensibles / regulación: ninguno (uso doméstico, sin cuentas ni datos personales). Supuesto del Orquestador.
- Alcance: **solo TVs propias** — instalación por ADB, sin publicar en Play Store.
- Plazo: **MVP usable y visible** lo antes posible, luego iterar.
- **Requisito duro del usuario:** respaldo completo y restauración garantizada del estado original de TV Santiago antes de tocar nada.
- Criticidad: media (por el requisito de restauración) → tope de iteraciones: 3. Supuesto del Orquestador.
- Cobertura mínima de pruebas: 70% en lógica (por defecto).

## 3. Ruta y modo vigentes
- **Tarea actual:** diseñar y construir launcher propio (MVP) para TV Santiago → **PROYECTO** (ceremonia completa)
- **Modo de autonomía:** 🤝 semiautomático

## 4. Stack
- UI — Kotlin + Jetpack Compose for TV — ADR-001
- Persistencia — Jetpack DataStore — TDD §7
- Botón Home — accesibilidad por defecto, híbrido por PoC — ADR-002
- Operación — ADB (platform-tools) + scripts PowerShell — TDD §7

## 5. Decisiones vigentes (ADR)
- ADR-001 — Kotlin + Compose for TV — docs/adr/ADR-001-stack-kotlin-compose-tv.md
- ADR-002 — Botón Home híbrido por PoC — docs/adr/ADR-002-boton-home-hibrido.md

## 6. Estado por fases
| Fase | Estado | Evidencia |
|---|---|---|
| Viabilidad | aprobada — VIABLE CON CONDICIONES (4 condiciones antes de Deploy) | docs/01-viabilidad.md |
| Requerimientos | aprobada — PRD v0.2 aprobado por el usuario | docs/02-prd.md, docs/03-historias.md |
| Arquitectura | aprobada — TDD v0.1, ADR-001/002, STRIDE inicial; variante final de ADR-002 pendiente de la PoC | docs/05-tdd.md, docs/adr/ |
| UX/UI | aprobada por el usuario — mockups v1, spec v0.1, tokens | docs/ux/pantalla-inicio.md, design/tokens.json |
| Construcción | MVP v0.1.0 construido en feature/mvp-launcher — 17/17 pruebas | app/, docs/07-revision-construccion.md |
| Revisión | aprobada con observaciones no bloqueantes | docs/07-revision-construccion.md |
| QA | aprobada — 17/17 unitarias + lista en dispositivo validada por el usuario con control | docs/07-revision-construccion.md |
| Seguridad | aprobada — checklist firmado, riesgos 8 y 9 aceptados | docs/08-seguridad.md |
| Documentación | aprobada — README, runbook con rescate (condición 4 ✔), CHANGELOG, scripts | README.md, docs/runbook.md, CHANGELOG.md, scripts/ |
| Deploy | pendiente — respaldo hecho (condición 1 ✔) | backups/tv-santiago/2026-10-26/ |
| Operación | no aplica en MVP | — |

## 7. Especialistas
- **Activos:** `performance-engineer` — el onn. Box tiene hardware modesto; el launcher debe ser fluido y ligero con control remoto.
- **Descartados:** `dba-data-architect` — solo almacenamiento trivial (preferencias locales); `bi-dashboards` — sin reportería; `containers-specialist` — no hay despliegue contenerizado; `cloud-architect` — sin nube; `infra-networks-sre` — red doméstica simple, la conexión ADB la cubre DevOps; `ai-product-engineer` — sin capa de IA en el alcance.

## 8. Integraciones
- **Activas:** GitHub (usuario andresvd95) — por confirmar si se crea repo.
- **Ausentes:** Jira/Confluence, SonarQube, Figma — se suplen con artefactos locales (tokens de diseño en el repo).
- **Figma:** no aplica
- **Capa de cumplimiento:** no aplicada — por proponer al crear el repo.
- **Herramientas externas usadas:** ninguna

## 9. Presupuesto de tokens
- **Presupuesto:** sin límite explícito (prioridad: MVP rápido)

## 10. Riesgos y bloqueos
- ADB resuelto: platform-tools 37.0.1 en %LOCALAPPDATA%\Android\platform-tools; TV vinculada por depuración inalámbrica (el puerto de conexión cambia; se descubre por mDNS).
- Google TV puede reabrir su launcher con el botón Home — impacto en la experiencia — se resuelve deshabilitando (no desinstalando) el launcher de stock por ADB, reversible.
- Restauración — impacto crítico — nunca desinstalar paquetes del sistema; solo `disable` reversible; respaldo previo de lista de paquetes, launcher por defecto y ajustes.

## 11. Bitácora (últimas 10)
- 2026-10-26 Descubiertos 2 onn. 4K Streaming Box en la red (192.168.1.8 TV Santiago, 192.168.1.17 TV VD).
- 2026-10-26 Intake completado: PROYECTO, semiautomático, MVP, piloto en TV Santiago, respaldo obligatorio.
- 2026-10-26 Viabilidad: VIABLE CON CONDICIONES (respaldo verificado, restauración probada, solo cambios reversibles, rescate documentado).
- 2026-10-26 PRD v0.1 + 9 historias (5 Must). Usuario: interfaz muy limpia, apps arriba sin recomendaciones, diagramación estilo Apple iPhone (solo layout, nada de íconos/marca).
- 2026-10-26 PRD v0.2: entran recomendaciones (debajo de apps), acceso a Play, perfiles y notificaciones de Google TV (HU-010..013, Must). Solo TV Santiago. Viabilidad anexo A: riesgos 6–7 altos + condición 5 (prueba de concepto en la TV antes de construir).
- 2026-10-26 Arquitectura: usuario eligió Kotlin+Compose TV (ADR-001) y Home híbrido por PoC (ADR-002). TDD v0.1 con STRIDE. Siguiente: prerrequisitos (ADB en TV y PC) + respaldo + PoC en TV Santiago.
- 2026-10-26 ADB instalado y TV Santiago vinculada. Respaldo completo en backups/tv-santiago/2026-10-26 (Android 14/SDK 34, HOME=launcherx, Netflix ya estaba deshabilitado). Condición 1 de viabilidad cumplida. Siguiente: PoC.
- 2026-10-26 PoC en TV Santiago: perfiles ✔, notificaciones ✔ (vía accesibilidad), Ajustes/Play ✔, Home por accesibilidad ✔ (28–217 ms). Recomendaciones de otras apps ✘ (permiso solo de sistema) → HU-010 escalada. ADR-002 → variante B. Informe: docs/06-poc-informe.md.
- 2026-10-26 Usuario: HU-010 → fila de Recientes. Home: corregido caso de pulsaciones rápidas con reintento (estrés 2/2 OK). Pendiente: juicio visual del parpadeo + prueba de restauración.
- 2026-10-26 Home sin destello con filtrado de tecla (validado por usuario). Restauración probada: TV idéntica al respaldo (condición 2 ✔). Condiciones 1,2,3,5 ✔; 4 (runbook) pendiente. ADR-002 → B reforzada. Siguiente: UX/UI.
- 2026-10-26 Construcción: repo git (rama feature/mvp-launcher, sin commits aún). MVP Kotlin+Compose TV: Apps, Recientes, barra superior, menú Mover/Fijar/Ocultar, HomeInterceptor 3 capas. 17/17 pruebas. Revisión aprobada con 3 observaciones bajas.
- 2026-10-26 MVP instalado en TV Santiago (dev.onn.launcher + accesibilidad + appops GET_USAGE_STATS). Corregidos en QA visual: escala (tokens a 960x540 dp), reloj sin p. m., Configuración fuera de Apps, filas hasta el borde, foco inicial con reintento. Observación: los banners tardan unos segundos en la primera carga.
- 2026-10-26 QA en TV aprobada por el usuario. Seguridad aprobada (sin INTERNET). Documentación: README, runbook, CHANGELOG 0.1.0, scripts de operación. Condiciones de viabilidad 1–5 ✔. Siguiente: commit + release v0.1.0.
- 2026-10-26 MVP v0.1.0 subido: PR andresvd95/OnnTvInterface#1 (feature/mvp-launcher → main), abierto.
- 2026-10-26 Nueva tarea → **FEATURE**: rediseño con distribución tipo app Apple TV (panel lateral que se oculta, portada de la app con foco con carrusel, tarjetas con título). Rama feature/ui-apple-tv (desde feature/mvp-launcher). Spec UX v0.2 + PRD v0.4 (HU-014..016) esperando aprobación.
- 2026-10-27 Rediseño Apple TV construido (panel lateral, portada, tarjetas) + HU-017 barra superior (reloj, Wi-Fi, Bluetooth, temporizador propio, audio). 27/27 pruebas. Instalado en TV Santiago; panel y portada verificados con capturas; barra superior pendiente de captura (TV en reposo). Lección: force-stop desactiva el servicio de accesibilidad (runbook).
- 2026-10-27 v1.0.0: release firmado con clave propia (fuera del repo), R8 1,7 MB. Corregidos en dispositivo: cierre al arrancar por R8 (regla keep), contraste de la barra superior. Instalado en TV Santiago (se reinstaló: firmas distintas).
