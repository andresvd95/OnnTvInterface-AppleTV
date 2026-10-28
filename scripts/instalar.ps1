# Instala o actualiza ONN Launcher en la TV y lo deja activo (HU-003, HU-013, HU-019, HU-020).
param([string]$Apk = (Join-Path $PSScriptRoot "..\app\build\outputs\apk\debug\app-debug.apk"))
. (Join-Path $PSScriptRoot "_comun.ps1")
if (-not (Test-Path $Apk)) { throw "No existe el APK: $Apk" }
$s = Connect-Tv
& $Script:Adb -s $s install -r $Apk
Invoke-Tv $s "appops set $Script:Pkg GET_USAGE_STATS allow" | Out-Null
Add-OurA11y $s
Invoke-Tv $s "cmd notification allow_listener $Script:MediaListener" | Out-Null
Invoke-Tv $s "am start -n $Script:Pkg/.ui.HomeActivity" | Out-Null
Write-Host "Listo. Accesibilidad: $((Invoke-Tv $s 'settings get secure enabled_accessibility_services').Trim())"