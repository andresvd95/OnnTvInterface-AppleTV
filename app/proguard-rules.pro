# Reglas de R8 para ONN Launcher.
# Actividad y servicio de accesibilidad se conservan por el manifest; Compose y DataStore traen sus propias reglas.

# lifecycle-runtime-compose 2.8 busca por reflexion el LocalLifecycleOwner de Compose UI 1.6;
# sin esta regla R8 lo elimina y el release se cierra al arrancar (visto en la TV, 2026-10-27).
-keep class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt { *; }
