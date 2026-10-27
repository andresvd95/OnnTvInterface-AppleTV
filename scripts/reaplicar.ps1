# Vuelve a activar el servicio y el permiso de Recientes (p. ej. tras una actualización de Google TV).
. (Join-Path $PSScriptRoot "_comun.ps1")
$s = Connect-Tv
Invoke-Tv $s "appops set $Script:Pkg GET_USAGE_STATS allow" | Out-Null
Add-OurA11y $s
Write-Host "Reaplicado."