# Runbook — ONN LAUNCHER en la TV

Requisitos: PC en la misma red, ADB en `%LOCALAPPDATA%\Android\platform-tools`, y en la TV **Opciones de desarrollador → Depuración inalámbrica** activada y vinculada con este PC (ver docs/06-poc-informe.md). Los scripts buscan solos el puerto de la TV.

Ejecutar desde PowerShell en la carpeta del proyecto (Windows bloquea scripts no firmados por defecto; esta forma lo permite solo para ese script):

```
powershell -ExecutionPolicy Bypass -File .\scripts\<script>.ps1
```

| Situación | Script | Qué hace |
|---|---|---|
| Antes de cualquier cambio | `respaldo.ps1` | Guarda paquetes, ajustes e inicio por defecto en `backups/tv/<fecha>/`. Solo lectura. |
| Instalar o actualizar | `instalar.ps1` | Instala el APK, concede estadísticas de uso, activa el servicio del botón Inicio y abre el launcher. |
| Tras una actualización de Google TV (vuelve a salir Google TV) | `reaplicar.ps1` | Reactiva el servicio y el permiso. |
| **RESCATE** / volver al original | `restaurar.ps1` | Quita nuestro servicio de accesibilidad (respeta otros), desinstala ONN Launcher y verifica que Google TV es el inicio. |

## Rescate sin PC (desde la TV)
1. **Ajustes → Sistema → Accesibilidad** → desactivar **ONN Launcher — botón Inicio**. Con eso Home vuelve a abrir Google TV.
2. Opcional: **Ajustes → Apps → ONN Launcher → Desinstalar**.

## Verificado
- Los comandos de `restaurar.ps1` son los mismos de la prueba de restauración del 2026-10-26: TV idéntica al respaldo (0 diferencias de paquetes).
- Los scripts se validaron en sintaxis; la primera ejecución real conviene hacerla con el usuario presente.

## Importante
- Nunca se desinstala ni deshabilita ningún paquete del sistema.
- Netflix (`com.netflix.ninja`) ya estaba deshabilitado antes del proyecto: la restauración lo deja así.
- Después de instalar, se recomienda **apagar la depuración inalámbrica** en la TV (TDD §8).

## Advertencia: no forzar el cierre
`adb shell am force-stop dev.onn.launcher` (o "Forzar detención" en Ajustes) **desactiva el servicio de accesibilidad**: Android lo quita de la lista de servicios activos y el botón Inicio vuelve a abrir Google TV. Si pasa, ejecutar `reaplicar.ps1`. (Visto en la TV, 2026-10-27.)

## Portada "Continuar" (v1.1.0)
`instalar.ps1` y `reaplicar.ps1` habilitan el servicio de notificaciones de ONN Launcher (`cmd notification allow_listener dev.onn.launcher/dev.onn.launcher.system.MediaListenerService`); `restaurar.ps1` lo quita. Sin PC: *Ajustes → Apps → Acceso especial de apps → Acceso a notificaciones → ONN Launcher*. Solo lee lo que se reproduce (título e imagen), no las notificaciones.

## Firma de release y publicación del APK
1. **Una sola vez:** `powershell -ExecutionPolicy Bypass -File .\scripts\crear-clave-release.ps1`. Crea la clave en `%USERPROFILE%\.onn-keys\onn-launcher-release.jks` y `keystore.properties` (ignorado por git). Contraseña solo ASCII. **Respalda la clave y la contraseña**: sin ellas la TV no acepta actualizaciones sin desinstalar.
2. Compilar `assembleRelease` (ver notas de build en docs/07-revision-construccion.md). Resultado: `app/build/outputs/apk/release/app-release.apk` (~1,6 MB, con R8).
3. **Primera vez en la TV:** el APK de depuración y el de release tienen firmas distintas; hay que desinstalar el de depuración (se pierden favoritas y ocultas) y luego `instalar.ps1 -Apk <ruta del release>`.
4. Publicar: GitHub Release con la etiqueta de la versión y el APK adjunto.
