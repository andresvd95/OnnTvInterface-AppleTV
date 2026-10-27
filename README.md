# ONN Launcher

Launcher doméstico para el onn. 4K Streaming Box **TV Santiago** (Google TV / Android 14). Sustituye las recomendaciones y anuncios de la pantalla de inicio por una vista limpia, pensada para usar a tres metros con el control remoto.

La interfaz toma como referencia la distribución de una app de TV moderna: panel lateral translúcido, portada de la aplicación seleccionada y una fila de tarjetas. No utiliza recursos, marca ni tipografías de terceros.

## Interfaz

Al iniciar se muestra un panel lateral con el perfil, la hora y los accesos del sistema. Al pasar a las tarjetas, el panel se oculta para dar protagonismo a la portada.

![Inicio con panel lateral y YouTube seleccionado](docs/media/home-panel-youtube.jpg)

La portada cambia con el foco y muestra el estado de uso de cada aplicación.

![Portada de Play Store](docs/media/home-play-store.jpg)

La navegación se realiza por completo con D-pad, OK, Atrás y Home. El foco se indica mediante escala y un anillo visible.

![Portada de Disney Plus con tarjeta enfocada](docs/media/home-disney-plus.jpg)

## Funcionalidades

- Aplicaciones recientes primero y el resto a continuación, sin duplicados.
- Portada con arte de la app, botón **Abrir** y carrusel automático de recientes cuando no hay interacción.
- Panel lateral con Perfil, Buscar, Inicio, Notificaciones, Google Play, Ajustes y aplicaciones fijadas.
- Mantener OK sobre una app para moverla, fijarla o quitarla de favoritos, u ocultarla.
- Accesos a las funciones nativas de Google TV: perfiles, notificaciones, Play Store y ajustes.
- El botón Home se intercepta mediante un servicio de accesibilidad sin desactivar Google TV.
- Respeta la opción del sistema para reducir animaciones.

## Tecnología

- Kotlin y Jetpack Compose for TV
- Android SDK 34, mínimo Android 12 (API 31)
- DataStore para preferencias locales
- UsageStats para ordenar aplicaciones recientes
- Sin permisos de Internet, cuentas propias ni recolección de datos

## Uso

En la TV, usa las flechas para navegar y OK para abrir una aplicación. Desde la primera tarjeta, pulsa izquierda —o Atrás desde el contenido— para volver a mostrar el panel lateral.

Para instalar, actualizar, reaplicar tras una actualización de Google TV, crear un respaldo o volver al launcher original, consulta el [runbook](docs/runbook.md).

## Compilación

El proyecto se compila con Gradle. En Windows, la ruta de este repositorio contiene caracteres no ASCII; consulta las [notas de compilación](docs/07-revision-construccion.md) para preparar una unidad virtual con `subst` antes de ejecutar las tareas de Gradle.

## Documentación

| Tema | Documento |
| --- | --- |
| Estado actual | [project-state.md](project-state.md) |
| Viabilidad | [docs/01-viabilidad.md](docs/01-viabilidad.md) |
| Requerimientos e historias | [docs/02-prd.md](docs/02-prd.md) · [docs/03-historias.md](docs/03-historias.md) |
| UX y tokens | [docs/ux/pantalla-inicio.md](docs/ux/pantalla-inicio.md) · [design/tokens.json](design/tokens.json) |
| Arquitectura | [docs/05-tdd.md](docs/05-tdd.md) · [docs/adr/](docs/adr/) |
| Prueba de concepto | [docs/06-poc-informe.md](docs/06-poc-informe.md) |
| Revisión y QA | [docs/07-revision-construccion.md](docs/07-revision-construccion.md) |
| Seguridad | [docs/08-seguridad.md](docs/08-seguridad.md) |

## Estado

La aplicación se encuentra en la versión **0.1.0**; la interfaz v0.2 está en desarrollo. La validación de navegación, accesos del sistema, rendimiento e intercepción real del botón Home debe completarse en el dispositivo objetivo.
