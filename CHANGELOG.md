# Changelog

Formato basado en Keep a Changelog; versiones SemVer.

## [1.1.0] — 2026-10-27
### Añadido
- Portada premium: la app con foco en grande (imagen a la derecha fundida con el fondo y luz ambiente con su color) y, cuando la app lo expone, su contenido real con **Continuar**: título, episodio o artista e imagen de lo último que reprodujo (HU-020).
- Ventana de Recientes debajo de las apps, con máximo 3 (HU-019).
### Cambiado
- La fila de Apps sigue el orden que elijas: Mover funciona en cualquier app con ← → (HU-018). El uso reciente ya no cambia el orden.
- La portada ya no rota entre las recientes.
### Corregido
- Atrás con el panel lateral abierto lo cierra y lleva a las apps.

## [1.0.0] — 2026-10-27
### Añadido
- Distribución tipo app Apple TV: panel lateral de vidrio que se oculta al entrar al contenido, portada de la app con foco con carrusel de Recientes y tarjetas con título y subtítulo (HU-014, HU-015).
- Barra superior con reloj y accesos rápidos a Wi-Fi, Bluetooth, temporizador de apagado y salida de audio (HU-017).
- Buscar en el panel cuando la TV lo permite (HU-016).
- Firma de release con clave propia; APK de release con R8 (~1,6 MB).
### Cambiado
- Mover apps se hace en "Mis apps" del panel; el arte de las apps se guarda en memoria para cargar más rápido.
### Corregido
- La fila de tarjetas vuelve al inicio cuando reaparece el panel.
- Script de clave: rutas con caracteres no ASCII en keystore.properties.
- Release: el build reducido con R8 se cerraba al arrancar (regla para LocalLifecycleOwner).
- Contraste del reloj y los accesos sobre portadas claras (velo superior).
## [0.1.0] — 2026-10-26 — MVP
### Añadido
- Pantalla de inicio: reloj y fecha, cápsula con Notificaciones, Google Play, Ajustes y Perfil (HU-004, HU-007, HU-011, HU-012, HU-013).
- Fila de Apps arriba, sin recomendaciones; foco inicial en la primera app; nombre visible solo en la app con foco (HU-001, HU-002).
- Menú al mantener OK: Mover, Fijar arriba / Quitar de arriba, Ocultar (HU-006, HU-008).
- Fila de Recientes con las apps usadas últimamente, máximo 10 (HU-010).
- Botón Inicio tomado por servicio de accesibilidad: tecla, ventana y reintento, sin destello (HU-003, ADR-002).
- Scripts de respaldo, instalación, reaplicación y restauración (HU-005).
### Pendiente (no bloqueante)
- Generar Tokens.kt desde design/tokens.json; respetar "Quitar animaciones"; punto de notificaciones pendientes; HU-009 (fondos).
