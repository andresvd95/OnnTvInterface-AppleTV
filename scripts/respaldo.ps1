# Respaldo de solo lectura del estado de la TV (paquetes, ajustes, inicio por defecto). No cambia nada en la TV.
. (Join-Path $PSScriptRoot "_comun.ps1")
$s = Connect-Tv
$d = Join-Path $PSScriptRoot ("..\backups\tv\" + (Get-Date -Format "yyyy-MM-dd"))
New-Item -ItemType Directory -Force $d | Out-Null
$cmds = [ordered]@{
  "getprop.txt" = "getprop"; "packages-all.txt" = "pm list packages -f -u --show-versioncode"
  "packages-disabled.txt" = "pm list packages -d"; "packages-thirdparty.txt" = "pm list packages -3 --show-versioncode"
  "home-default.txt" = "cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME"
  "settings-global.txt" = "settings list global"; "settings-secure.txt" = "settings list secure"; "settings-system.txt" = "settings list system"
}
foreach ($k in $cmds.Keys) { Invoke-Tv $s $cmds[$k] | Out-File -Encoding utf8 (Join-Path $d $k) }
Write-Host "Respaldo en $d"