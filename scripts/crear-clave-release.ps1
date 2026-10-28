# Crea la clave de firma de release de ONN Launcher. EJECÚTALO TÚ: te pide las contraseñas.
# La clave queda FUERA del repo, en %USERPROFILE%\.onn-keys. Respáldala junto con su contraseña:
# sin ella no podrás publicar actualizaciones que la TV acepte sin desinstalar.
$ErrorActionPreference = "Stop"
$keytool = "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe"
if (-not (Test-Path $keytool)) { throw "No se encontró keytool en $keytool" }
$dir = Join-Path $env:USERPROFILE ".onn-keys"
$jks = Join-Path $dir "onn-launcher-release.jks"
if (Test-Path $jks) { throw "Ya existe una clave en $jks. No se sobrescribe." }
New-Item -ItemType Directory -Force $dir | Out-Null

Write-Host "keytool te pedirá una contraseña (mínimo 6 caracteres) y que la repitas." -ForegroundColor Cyan
Write-Host "Usa solo letras SIN tildes ni ñ, números y símbolos: Gradle lee el archivo en Latin-1." -ForegroundColor Cyan
& $keytool -genkeypair -v -storetype PKCS12 -keystore $jks -alias onn-launcher -keyalg RSA -keysize 4096 -validity 10000 -dname "CN=ONN Launcher, O=Personal, C=CO"
if ($LASTEXITCODE -ne 0) { throw "keytool falló." }

$sec = Read-Host -AsSecureString "Escribe otra vez la misma contraseña para que Gradle pueda firmar"
$plain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec))
$props = Join-Path $PSScriptRoot "..\keystore.properties"
@"
storeFile=$([regex]::Replace(($jks -replace '\\','/'), '[^\x00-\x7F]', { param($m) '\u{0:x4}' -f [int][char]$m.Value }))
storePassword=$plain
keyAlias=onn-launcher
keyPassword=$plain
"@ | Set-Content -Encoding ascii $props
Write-Host "Listo. Clave: $jks" -ForegroundColor Green
Write-Host "keystore.properties creado (no se sube a git). RESPALDA la clave y la contraseña." -ForegroundColor Yellow