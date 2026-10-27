# PRD — ONN LAUNCHER

> Dueño: `requirements-analyst` · Aprobación: usuario (modo semiautomático)

**Versión:** 0.4 · **Fecha:** 2026-10-26 · **Estado:** borrador (v0.4: distribución tipo app Apple TV — panel lateral, portada de la app con foco, tarjetas con título; HU-014..016)

## 1. Problema y objetivo
- **Problema:** la interfaz de Google TV en TV Santiago llena la pantalla de recomendaciones y contenido que nadie pidió, y las apps quedan en segundo plano.
- **Objetivo:** una pantalla de inicio **muy limpia** donde lo primero, arriba, sean las apps, sin recomendaciones encima; las recomendaciones van debajo. La diagramación toma como referencia el lenguaje actual de Apple en iPhone: mucho aire, jerarquía clara, capas translúcidas y esquinas redondeadas. Se toma **solo la diagramación**: ni íconos, ni marca, ni nada más de Apple. *(Palabras del usuario; la interpretación del estilo es del Orquestador y la concreta UX/UI.)*
- **Métricas de éxito:**
  | Métrica | Objetivo | Cómo se mide |
  |---|---|---|
  | Clics para abrir una app favorita desde Home | ≤ 2 (navegar + OK) | Prueba manual con control remoto |
  | Tiempo de Home a pantalla visible | < 1,5 s *(supuesto)* | Medición en TV Santiago |
  | Contenido por encima de la fila de apps | 0 | Inspección visual |

## 2. Usuarios y actores
| Actor | Descripción | Necesidad principal |
|---|---|---|
| Persona de la casa | Usa TV Santiago con el control remoto | Encender y abrir su app rápido, sin distracciones |
| Dueño / administrador | El usuario; instala y mantiene desde el PC | Instalar, configurar y poder restaurar el original en cualquier momento |

## 3. Alcance (MoSCoW)
| Prioridad | Historia |
|---|---|
| Must | HU-001 — Apps arriba al iniciar, sin recomendaciones |
| Must | HU-002 — Navegar y abrir apps con el control remoto |
| Must | HU-003 — Ser la pantalla de inicio (botón Home) |
| Must | HU-004 — Acceso a Ajustes del sistema |
| Must | HU-005 — Respaldo y restauración del estado original |
| Should | HU-006 — Elegir y ordenar las apps de la fila superior |
| Should | HU-007 — Reloj y fecha discretos |
| Could | HU-008 — Ocultar apps que no se usan |
| Could | HU-009 — Cambiar el fondo |
| Must | HU-014 — Portada grande de la app con foco, con carrusel de Recientes |
| Must | HU-015 — Panel lateral de vidrio (perfil, navegación, Mis apps) que se oculta al entrar al contenido |
| Could | HU-016 — Buscar (abre la búsqueda de Google TV; sujeto a verificación) |
| Must | HU-010 — Fila de **Recientes** (apps usadas últimamente) **debajo** de la fila de apps *(reemplaza a las recomendaciones de contenido: la PoC demostró que Android no deja leer las de otras apps; decisión del usuario 2026-10-26)* |
| Must | HU-011 — Acceso a Google Play (tienda) |
| Must | HU-012 — Perfiles de Google TV |
| Must | HU-013 — Notificaciones del sistema |
| Won't | Perfiles o notificaciones propios del launcher, búsqueda por voz propia, TV VD, publicar el launcher en Play Store |

## 4. Fuera de alcance
- Recomendaciones **encima** o mezcladas con la fila de apps: las apps siempre van primero, arriba.
- Íconos, marca, tipografía propietaria o cualquier recurso de Apple: solo se toma la diagramación.
- **TV VD:** el proyecto es solo para TV Santiago.
- Publicar el launcher en Play Store o distribuirlo a terceros.
- Perfiles y notificaciones propios: se usan los de Google TV.
- Reemplazar Ajustes, búsqueda o el asistente de Google: se abren los del sistema.

## 5. Requisitos no funcionales
- **Rendimiento:** de Home a pantalla visible < 1,5 s; navegación fluida (objetivo 60 fps) en el onn. 4K Box. *(Supuestos; Performance los valida.)*
- **Interfaz a 3 m (10-foot UI):** textos legibles desde el sofá; foco siempre visible y evidente; todo operable solo con D-pad + OK + Atrás + Home.
- **Seguridad y privacidad:** sin cuentas, sin red propia, sin recolección de datos. Permisos mínimos.
- **Accesibilidad:** WCAG 2.2 AA adaptado a TV (contraste del foco y del texto sobre fondos translúcidos).
- **Compatibilidad:** onn. 4K Streaming Box con Google TV (Android 12+ *(supuesto, se verifica en el respaldo)*), salidas 1080p y 4K.
- **Reversibilidad:** ningún cambio en la TV que no se pueda deshacer con un comando.

## 6. Dependencias e integraciones
- Sistema Android TV: lista de apps instaladas y apertura de apps — interfaz estándar del sistema.
- ADB desde el PC del usuario: instalación, respaldo y restauración — solo en mantenimiento, no en uso diario.

## 7. Supuestos y riesgos
| Tipo | Descripción | Mitigación |
|---|---|---|
| Decisión del usuario | Debajo de las apps: recomendaciones de las apps instaladas; además reloj/fecha y accesos de sistema (Ajustes, Play, perfiles, notificaciones) | El usuario lo valida en los mockups |
| Riesgo alto | Android restringe a launchers de terceros la lectura de recomendaciones de otras apps, y perfiles/notificaciones viven dentro del launcher de Google TV | Anexo A de `01-viabilidad.md`; el Arquitecto lo resuelve en ADR-001 |
| Supuesto | "iPhone 18" se toma como el lenguaje visual actual de Apple (capas translúcidas tipo vidrio, espaciado amplio, jerarquía tipográfica) | El usuario lo valida en los mockups |
| Riesgo | Efectos translúcidos (desenfoque) pueden ser costosos en el hardware del onn. Box | Performance define una versión ligera del efecto |
| Riesgo | Ver riesgos 1–5 de `01-viabilidad.md` | Ver mitigaciones allí |

## 8. Casos borde
- Se instala o desinstala una app con el launcher abierto → la lista se actualiza sola.
- App sin banner de TV → se muestra su ícono normal con el mismo tamaño y estilo.
- Muchas apps (> 1 fila) → la fila superior desplaza horizontalmente; el resto aparece en "Todas las apps".
- Ninguna app instalada (aparte del sistema) → mensaje limpio y acceso a Ajustes.
- El launcher se cierra por un error → Android lo vuelve a abrir; si falla repetidamente, el comando de rescate restaura Google TV.

## 9. Historias
Detalle con criterios de aceptación en [`03-historias.md`](03-historias.md).
