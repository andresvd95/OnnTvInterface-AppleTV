# RESCATE / RESTAURACIÓN (HU-005, condición 4): deja la TV como en el respaldo del 2026-10-26.
# Quita nuestro servicio de accesibilidad, desinstala ONN Launcher y verifica que Google TV vuelve a ser el inicio.
. (Join-Path $PSScriptRoot "_comun.ps1")
$s = Connect-Tv
Remove-OurA11y $s
& $Script:Adb -s $s uninstall $Script:Pkg | Out-Null
Invoke-Tv $s "input keyevent HOME" | Out-Null
$inicio = (Invoke-Tv $s "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME") -join " "
$installed = [bool](Invoke-Tv $s "pm list packages $Script:Pkg")
Write-Host "Inicio por defecto: $inicio"
Write-Host "ONN Launcher instalado: $installed"
if ($inicio -match $Script:StockHome -and -not $installed) { Write-Host "Restauración correcta." -ForegroundColor Green } else { Write-Warning "Revisar: la TV no quedó como en el respaldo." }