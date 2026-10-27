# Funciones compartidas por los scripts de ONN LAUNCHER. No ejecutar directamente.
$ErrorActionPreference = "Stop"
$Script:Adb = Join-Path $env:LOCALAPPDATA "Android\platform-tools\adb.exe"
$Script:TvIp = "192.168.1.8"
$Script:Pkg = "dev.onn.launcher"
$Script:A11y = "dev.onn.launcher/dev.onn.launcher.system.HomeInterceptorService"
$Script:StockHome = "com.google.android.apps.tv.launcherx"

function Connect-Tv {
    if (-not (Test-Path $Script:Adb)) { throw "No se encontró ADB en $Script:Adb" }
    $serial = & $Script:Adb devices | Select-String "$([regex]::Escape($Script:TvIp)):\d+\s+device" | ForEach-Object { ($_ -split "\s+")[0] } | Select-Object -First 1
    if (-not $serial) {
        $svc = & $Script:Adb mdns services | Select-String "_adb-tls-connect.*$([regex]::Escape($Script:TvIp)):(\d+)"
        if (-not $svc) { throw "TV Santiago no anuncia depuración inalámbrica. Actívala en Opciones de desarrollador." }
        & $Script:Adb connect "$($Script:TvIp):$($svc.Matches[0].Groups[1].Value)" | Out-Null
        $serial = & $Script:Adb devices | Select-String "$([regex]::Escape($Script:TvIp)):\d+\s+device" | ForEach-Object { ($_ -split "\s+")[0] } | Select-Object -First 1
    }
    if (-not $serial) { throw "No se pudo conectar a TV Santiago." }
    Write-Host "Conectado a $serial"
    return $serial
}

function Invoke-Tv([string]$Serial, [string]$Cmd) { & $Script:Adb -s $Serial shell $Cmd }

# Quita solo nuestro servicio de la lista de accesibilidad (respeta otros que el usuario active).
function Remove-OurA11y([string]$Serial) {
    $cur = (Invoke-Tv $Serial "settings get secure enabled_accessibility_services").Trim()
    $rest = @($cur -split ":" | Where-Object { $_ -and $_ -ne "null" -and $_ -ne $Script:A11y })
    if ($rest.Count -eq 0) {
        Invoke-Tv $Serial "settings delete secure enabled_accessibility_services" | Out-Null
        Invoke-Tv $Serial "settings put secure accessibility_enabled 0" | Out-Null
    } else {
        Invoke-Tv $Serial "settings put secure enabled_accessibility_services $($rest -join ':')" | Out-Null
    }
}

function Add-OurA11y([string]$Serial) {
    $cur = (Invoke-Tv $Serial "settings get secure enabled_accessibility_services").Trim()
    $list = @($cur -split ":" | Where-Object { $_ -and $_ -ne "null" })
    if ($list -notcontains $Script:A11y) { $list += $Script:A11y }
    Invoke-Tv $Serial "settings put secure enabled_accessibility_services $($list -join ':')" | Out-Null
    Invoke-Tv $Serial "settings put secure accessibility_enabled 1" | Out-Null
}