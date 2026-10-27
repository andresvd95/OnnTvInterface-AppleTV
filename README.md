# ONN LAUNCHER

Pantalla de inicio propia y limpia para el onn. 4K Streaming Box "TV Santiago" (Google TV, Android 14). Las apps van arriba, debajo la fila de Recientes y en la barra superior el reloj y los accesos a Notificaciones, Google Play, Ajustes y Perfil. Sin recomendaciones ni anuncios.

- **Versión:** 0.1.0 (MVP)
- **Stack:** Kotlin + Jetpack Compose for TV (ADR-001)
- **Botón Inicio:** servicio de accesibilidad que lo toma sin desactivar Google TV (ADR-002)
- **Permisos:** listar apps y estadísticas de uso. Sin Internet.

## Uso diario
Flechas para moverse, OK para abrir. Mantener OK sobre una app: Mover, Fijar arriba / Quitar de arriba, Ocultar.

## Operación (desde el PC)
Ver [docs/runbook.md](docs/runbook.md): instalar, actualizar, reaplicar tras una actualización de Google TV, respaldo y **rescate** (volver a Google TV con un comando).

## Compilar
Ver [docs/07-revision-construccion.md](docs/07-revision-construccion.md), sección "Notas de build": la ruta del usuario de Windows tiene "é" y Gradle necesita unidades virtuales ASCII.

## Documentos del proyecto
| Fase | Documento |
|---|---|
| Estado vivo | [project-state.md](project-state.md) |
| Viabilidad | [docs/01-viabilidad.md](docs/01-viabilidad.md) |
| Requerimientos | [docs/02-prd.md](docs/02-prd.md) · [docs/03-historias.md](docs/03-historias.md) |
| Arquitectura | [docs/05-tdd.md](docs/05-tdd.md) · [docs/adr/](docs/adr/) |
| Prueba de concepto | [docs/06-poc-informe.md](docs/06-poc-informe.md) |
| UX/UI | [docs/ux/pantalla-inicio.md](docs/ux/pantalla-inicio.md) · [design/tokens.json](design/tokens.json) |
| Revisión y QA | [docs/07-revision-construccion.md](docs/07-revision-construccion.md) |
| Seguridad | [docs/08-seguridad.md](docs/08-seguridad.md) |
