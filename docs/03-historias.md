# Historias de usuario — ONN LAUNCHER (MVP)

**Ruta:** PROYECTO · **Fecha:** 2026-10-26 · Dueño: `requirements-analyst`

Definición de terminado común: criterios probados por QA (tests derivados de estas historias) · revisión aprobada · docs actualizadas · sin secretos ni valores mágicos.

---

## HU-001 — Apps arriba al iniciar, sin recomendaciones encima · **Must**
**Como** persona de la casa **quiero** ver mis apps arriba apenas aparece la pantalla de inicio **para** abrir lo que quiero sin distracciones.

1. **Dado** que el launcher está abierto **cuando** se muestra la pantalla **entonces** solo se muestran apps (portada de la app con foco, tarjetas y Mis apps): ningún contenido de terceros. *(v0.4: la distribución pasa a la de la app Apple TV; ver HU-014/015.)*
2. **Dado** cualquier estado **cuando** se muestra la pantalla **entonces** no aparece ninguna recomendación, póster, anuncio ni fila de contenido por encima de la fila de apps.
3. **Dado** que se abre el launcher **cuando** termina de cargar **entonces** el foco está en **Inicio** del panel lateral, y con → pasa a la primera tarjeta.

**Casos borde:** app sin banner de TV → ícono con el mismo tamaño y estilo. Más apps de las que caben → la fila desplaza horizontalmente.

## HU-002 — Navegar y abrir apps con el control · **Must**
**Como** persona de la casa **quiero** moverme con las flechas y abrir con OK **para** usarlo solo con el control remoto.

1. **Dado** el foco en una app **cuando** pulso ← / → **entonces** el foco pasa a la app vecina y se ve claramente cuál está seleccionada.
2. **Dado** el foco en una app **cuando** pulso OK **entonces** se abre esa app.
3. **Dado** que estoy dentro de una app **cuando** pulso Atrás hasta salir **entonces** vuelvo al launcher con el foco en la última app usada.
4. **Dado** el foco en la fila superior **cuando** pulso ↓ **entonces** el foco baja a la siguiente sección.

**Casos borde:** en el borde de la fila no hace nada o se detiene, nunca salta a una sección sin aviso. Si la app ya no existe, se quita de la lista sin error.

## HU-003 — Ser la pantalla de inicio · **Must**
**Como** dueño **quiero** que el botón Home siempre abra mi launcher **para** que nadie vuelva a la interfaz de Google TV.

1. **Dado** el launcher instalado y configurado **cuando** se pulsa Home desde cualquier app **entonces** se abre el launcher.
2. **Dado** que la TV se reinicia **cuando** termina de encender **entonces** aparece el launcher.
3. **Dado** que se instala una app nueva **cuando** vuelvo al launcher **entonces** la app aparece sin reiniciar nada.

**Casos borde:** si una actualización de Google TV reactiva su launcher, el script de "reaplicar" lo corrige con un comando.

## HU-004 — Acceso a Ajustes · **Must**
**Como** persona de la casa **quiero** un acceso visible a Ajustes **para** cambiar WiFi, sonido o pantalla sin la interfaz de Google.

1. **Dado** el launcher abierto **cuando** voy al acceso de Ajustes y pulso OK **entonces** se abren los Ajustes del sistema.
2. **Dado** que estoy en Ajustes **cuando** pulso Atrás o Home **entonces** vuelvo al launcher.

## HU-005 — Respaldo y restauración · **Must**
**Como** dueño **quiero** respaldar la TV antes de instalar nada y poder volver al original con un comando **para** no perder nunca el estado de fábrica.

1. **Dado** La TV conectada por ADB **cuando** ejecuto el respaldo **entonces** queda guardada en `backups/tv/AAAA-MM-DD/`: la lista completa de paquetes (habilitados y deshabilitados), el launcher por defecto, la versión de Android/build y los ajustes clave.
2. **Dado** el launcher propio fijado **cuando** ejecuto el comando de restauración **entonces** Google TV vuelve a ser la pantalla de inicio y nuestro launcher se desinstala.
3. **Dado** cualquier momento **cuando** reviso los cambios hechos en la TV **entonces** ninguno es un `uninstall` de paquetes del sistema.

## HU-006 — Elegir y ordenar las apps de arriba · **Should**
**Como** persona de la casa **quiero** fijar y ordenar mis apps favoritas en la fila superior **para** tener las que más uso primero.

1. **Dado** el foco en una app **cuando** mantengo OK presionado **entonces** aparece un menú con "Mover" y "Quitar de favoritas" (o "Añadir a favoritas").
2. **Dado** "Mover" activo **cuando** uso ← / → y confirmo con OK **entonces** la app queda en la nueva posición, y esa posición se mantiene después de reiniciar.

## HU-007 — Reloj y fecha · **Should**
**Como** persona de la casa **quiero** ver la hora y la fecha de forma discreta **para** no tener que buscarla.

1. **Dado** el launcher abierto **cuando** se muestra la pantalla **entonces** se ven la hora y la fecha en la zona superior, con el formato de la TV (es-419, 12 h).
2. **Dado** que cambia el minuto **cuando** el launcher está abierto **entonces** el reloj se actualiza.

## HU-008 — Ocultar apps · **Could**
**Como** dueño **quiero** ocultar apps que no se usan **para** mantener la pantalla limpia.

1. **Dado** el menú de una app **cuando** elijo "Ocultar" **entonces** la app deja de verse en el launcher, pero sigue instalada.
2. **Dado** apps ocultas **cuando** entro en "Apps ocultas" desde Ajustes del launcher **entonces** puedo volver a mostrarlas.

## HU-009 — Cambiar el fondo · **Could**
**Como** persona de la casa **quiero** elegir entre algunos fondos **para** personalizar la TV.

1. **Dado** los ajustes del launcher **cuando** elijo un fondo **entonces** se aplica al instante y se mantiene después de reiniciar.

## HU-010 — Fila de Recientes debajo de las apps · **Must** *(v0.3: reemplaza a "recomendaciones"; ver docs/06-poc-informe.md)*
**Como** persona de la casa **quiero** ver debajo de mis apps las que usé últimamente **para** volver rápido a lo que estaba viendo, sin que tape mis apps.

1. **Dado** apps usadas recientemente **cuando** se muestra la pantalla **entonces** aparece una fila "Recientes" **debajo** de la fila de apps, ordenada de la más reciente a la más antigua, con el mismo estilo limpio.
2. **Dado** el foco en una app de Recientes **cuando** pulso OK **entonces** se abre esa app.
3. **Dado** que todavía no hay uso registrado **cuando** se muestra la pantalla **entonces** la fila no aparece (sin huecos ni mensajes de error).

**Casos borde:** el propio launcher, Ajustes y apps ocultas no aparecen en Recientes. Una app desinstalada desaparece de la fila. Máximo 10 apps en la fila.
**Nota técnica:** requiere acceso a estadísticas de uso (PACKAGE_USAGE_STATS), que se concede por ADB al instalar; si no está concedido, la fila no se muestra.

## HU-011 — Acceso a Google Play · **Must**
**Como** persona de la casa **quiero** un acceso visible a la tienda **para** instalar apps nuevas.

1. **Dado** el launcher abierto **cuando** voy al acceso de Google Play y pulso OK **entonces** se abre Google Play.
2. **Dado** que instalo una app desde Play **cuando** vuelvo al launcher **entonces** la app nueva ya aparece.

## HU-012 — Perfiles de Google TV · **Must**
**Como** persona de la casa **quiero** ver y cambiar el perfil de Google TV desde el launcher **para** usar mis propias cuentas y recomendaciones.

1. **Dado** el launcher abierto **cuando** miro la zona superior **entonces** veo el perfil activo (avatar o inicial).
2. **Dado** el foco en el perfil **cuando** pulso OK **entonces** se abre el selector de perfiles del sistema de Google TV.
3. **Dado** que cambio de perfil **cuando** vuelvo al launcher **entonces** sigo en nuestro launcher (no vuelve la interfaz de Google TV).

## HU-013 — Notificaciones del sistema · **Must**
**Como** persona de la casa **quiero** abrir las notificaciones del sistema desde el launcher **para** no perderme avisos (actualizaciones, mensajes de apps).

1. **Dado** el launcher abierto **cuando** voy al acceso de notificaciones y pulso OK **entonces** se abre el panel de notificaciones de Google TV.
2. **Dado** notificaciones pendientes **cuando** se muestra la pantalla **entonces** el acceso muestra un indicador discreto. *(Supuesto; depende de lo que el sistema permita leer.)*

## HU-014 — Portada de la app con foco · **Must**
**Como** persona de la casa **quiero** ver en grande la app que estoy seleccionando **para** que la pantalla se sienta viva y clara, como en Apple TV, sin recomendaciones ajenas.

1. **Dado** el foco en una tarjeta **cuando** cambia el foco **entonces** la portada muestra esa app: fondo a pantalla completa con su arte desenfocado, su banner, su nombre y cuándo se usó por última vez.
2. **Dado** la portada visible **cuando** pulso ↑ y luego OK en **Abrir** **entonces** se abre la app de la portada.
3. **Dado** que nadie navega durante 8 s **cuando** hay Recientes **entonces** la portada rota entre ellas (máx. 5) con indicadores de puntos; al navegar, se detiene.
4. **Dado** "Quitar animaciones" activo **cuando** se muestra la portada **entonces** no rota sola.

**Casos borde:** app sin uso registrado → subtítulo "Nueva". App sin banner → ícono sobre su color.

## HU-015 — Panel lateral de vidrio · **Must**
**Como** persona de la casa **quiero** un panel a la izquierda con mi perfil, la hora, la navegación y mis apps **para** tener todo a mano sin ensuciar la pantalla.

1. **Dado** que abro el launcher **cuando** se muestra **entonces** el panel está visible con: perfil y hora, Buscar, Inicio, Notificaciones, Google Play, Ajustes y la sección "Mis apps" (favoritas en su orden).
2. **Dado** el foco en el panel **cuando** pulso → **entonces** el panel se oculta y el foco pasa a la primera tarjeta.
3. **Dado** el panel oculto **cuando** pulso ← en la primera tarjeta o Atrás **entonces** el panel vuelve.
4. **Dado** el foco en una app de "Mis apps" **cuando** pulso OK **entonces** se abre esa app.

## HU-016 — Buscar · **Could** *(sujeto a verificación en la TV)*
**Como** persona de la casa **quiero** un acceso a Buscar en el panel **para** buscar contenido con la búsqueda de Google TV.

1. **Dado** el foco en Buscar **cuando** pulso OK **entonces** se abre la búsqueda de Google TV.
2. **Dado** que la TV no permite abrirla desde nuestra app **cuando** se instala **entonces** el ítem Buscar no aparece.
## HU-017 — Reloj y accesos rápidos arriba · **Must** *(v0.5, 2026-10-27)*
**Como** persona de la casa **quiero** ver la hora y tener a mano Wi-Fi, Bluetooth, temporizador y salida de audio **para** no tener que entrar a Ajustes ni a la interfaz de Google TV.

1. **Dado** el launcher abierto (con el panel visible u oculto) **cuando** miro arriba a la derecha **entonces** veo la hora, la fecha y los 4 accesos.
2. **Dado** el foco en Wi-Fi **cuando** pulso OK **entonces** se abre la pantalla de red; si no hay conexión, el acceso dice "Sin red".
3. **Dado** el foco en Bluetooth **cuando** pulso OK **entonces** se abre la pantalla para vincular accesorios.
4. **Dado** el foco en Temporizador **cuando** elijo 30 min **entonces** el acceso muestra los minutos restantes y, al llegar a 0, la TV pasa a reposo.
5. **Dado** un temporizador activo **cuando** elijo Desactivado **entonces** se cancela y la TV no se apaga.
6. **Dado** el foco en Audio **cuando** pulso OK **entonces** se abre Pantalla y sonido, donde se elige la salida.

**Casos borde:** si el servicio de accesibilidad está apagado, el temporizador avisa que no puede apagar la TV. Al reiniciar la TV el temporizador se cancela.
