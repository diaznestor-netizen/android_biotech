param(
    [string]$AvdName = "Medium_Phone",
    [int]$BootTimeoutSeconds = 420,
    [string]$GpuMode = "swiftshader_indirect",
    [switch]$NoWindow,
    [int]$PostBootStabilizationSeconds = 30,
    [switch]$KeepEmulatorOpen
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$androidRoot = Join-Path $repoRoot "android"
$artifactsDir = Join-Path $repoRoot ".artifacts\projects-connected-cert"
$sdkRoot = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$adb = Join-Path $sdkRoot "platform-tools\adb.exe"
$emulator = Join-Path $sdkRoot "emulator\emulator.exe"
$avdHome = Join-Path $env:USERPROFILE ".android\avd"
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$gradleLog = Join-Path $artifactsDir "gradle-connected-$timestamp.log"
$adbLog = Join-Path $artifactsDir "adb-logcat-$timestamp.log"
$emulatorOut = Join-Path $artifactsDir "emulator-out-$timestamp.log"
$emulatorErr = Join-Path $artifactsDir "emulator-err-$timestamp.log"
$summary = Join-Path $artifactsDir "summary-$timestamp.txt"

New-Item -ItemType Directory -Force -Path $artifactsDir | Out-Null

function Write-Step([string]$message) {
    Write-Host "==> $message"
}

function Assert-Tool([string]$path, [string]$label) {
    if (-not (Test-Path $path)) {
        throw "$label no encontrado en $path"
    }
}

function Stop-PreviousEmulators {
    Write-Step "Cerrando instancias previas del emulador"
    & $adb devices | Out-Null
    $serials = & $adb devices | Select-String "^emulator-" | ForEach-Object { ($_ -split "\s+")[0] }
    foreach ($serial in $serials) {
        & $adb -s $serial emu kill | Out-Null
    }
    Get-Process emulator, qemu-system-x86_64 -ErrorAction SilentlyContinue | Stop-Process -Force
}

function Restart-Adb {
    Write-Step "Reiniciando adb"
    & $adb kill-server | Out-Null
    & $adb start-server | Out-Null
}

function Get-AvailableAvds {
    $env:ANDROID_AVD_HOME = $avdHome
    $env:ANDROID_SDK_HOME = $env:USERPROFILE
    $output = & $emulator -list-avds 2>&1
    return @($output | Where-Object { $_ -and $_.Trim() -ne "" })
}

function Start-Emulator {
    Write-Step "Lanzando AVD $AvdName con GPU $GpuMode"
    $env:ANDROID_AVD_HOME = $avdHome
    $env:ANDROID_SDK_HOME = $env:USERPROFILE
    $arguments = @(
        "-avd", $AvdName,
        "-wipe-data",
        "-no-snapshot-load",
        "-no-snapshot-save",
        "-no-boot-anim",
        "-gpu", $GpuMode,
        "-no-audio"
    )
    if ($NoWindow) {
        $arguments += "-no-window"
    }
    Start-Process -FilePath $emulator `
        -ArgumentList $arguments `
        -RedirectStandardOutput $emulatorOut `
        -RedirectStandardError $emulatorErr `
        -WindowStyle Hidden
}

function Wait-ForBoot {
    Write-Step "Esperando a que adb detecte el dispositivo"
    $deadline = (Get-Date).AddSeconds($BootTimeoutSeconds)
    $serial = $null
    $lastRecoveryAttempt = [datetime]::MinValue
    $bootGracePeriodSeconds = 120

    while ((Get-Date) -lt $deadline) {
        $elapsedSeconds = [int](((Get-Date) - ($deadline.AddSeconds(-1 * $BootTimeoutSeconds))).TotalSeconds)
        $adbDevices = & $adb devices
        $deviceLine = $adbDevices | Select-String "^emulator-.*\sdevice$" | Select-Object -First 1
        if ($deviceLine) {
            $serial = ($deviceLine.Line -split "\s+")[0]
            break
        }
        $offlineLine = $adbDevices | Select-String "^emulator-.*\soffline$" | Select-Object -First 1
        if ($offlineLine -and $elapsedSeconds -ge $bootGracePeriodSeconds -and ((Get-Date) - $lastRecoveryAttempt).TotalSeconds -ge 60) {
            $offlineSerial = ($offlineLine.Line -split "\s+")[0]
            Write-Step "Emulador $offlineSerial sigue offline tras $elapsedSeconds s; intentando adb reconnect offline"
            & $adb reconnect offline | Out-Null
            $lastRecoveryAttempt = Get-Date
        }
        Start-Sleep -Seconds 5
    }

    if (-not $serial) {
        throw "No apareció ningún emulador en estado device dentro de $BootTimeoutSeconds segundos."
    }

    Write-Step "Esperando sys.boot_completed=1 para $serial"
    while ((Get-Date) -lt $deadline) {
        $bootRaw = & $adb -s $serial shell getprop sys.boot_completed 2>$null
        $boot = if ($null -eq $bootRaw) { "" } else { ($bootRaw | Out-String).Trim() }
        if ($boot -eq "1") {
            & $adb -s $serial shell input keyevent 82 | Out-Null
            return $serial
        }
        if (-not $boot) {
            & $adb reconnect offline | Out-Null
        }
        Start-Sleep -Seconds 5
    }

    throw "El emulador $serial no alcanzó sys.boot_completed=1 dentro de $BootTimeoutSeconds segundos."
}

function Start-Logcat([string]$serial) {
    Write-Step "Capturando logcat en $adbLog"
    Start-Process -FilePath $adb `
        -ArgumentList "-s",$serial,"logcat","-d" `
        -RedirectStandardOutput $adbLog `
        -WindowStyle Hidden
}

function Wait-ForPostBootStabilization([string]$serial) {
    if ($PostBootStabilizationSeconds -le 0) {
        return
    }
    Write-Step "Esperando estabilización post-boot durante $PostBootStabilizationSeconds s"
    $deadline = (Get-Date).AddSeconds($PostBootStabilizationSeconds)
    while ((Get-Date) -lt $deadline) {
        $stateLine = (& $adb devices | Select-String "^$serial\sdevice$" | Select-Object -First 1)
        $bootRaw = & $adb -s $serial shell getprop sys.boot_completed 2>$null
        $boot = if ($null -eq $bootRaw) { "" } else { ($bootRaw | Out-String).Trim() }
        if (-not $stateLine -or $boot -ne "1") {
            throw "El emulador $serial perdió estabilidad durante la ventana post-boot."
        }
        Start-Sleep -Seconds 5
    }
}

function Invoke-ConnectedTests {
    Write-Step "Ejecutando :app:connectedDebugAndroidTest"
    Push-Location $androidRoot
    try {
        & ".\gradle-8.5\bin\gradle.bat" ":app:connectedDebugAndroidTest" *>&1 | Tee-Object -FilePath $gradleLog | Out-Null
        return [int]$LASTEXITCODE
    } finally {
        Pop-Location
    }
}

function Write-Summary([string]$serial, [int]$exitCode, [string[]]$availableAvds) {
    @(
        "timestamp=$timestamp"
        "avd_name=$AvdName"
        "gpu_mode=$GpuMode"
        "no_window=$NoWindow"
        "post_boot_stabilization_seconds=$PostBootStabilizationSeconds"
        "android_avd_home=$avdHome"
        "available_avds=$($availableAvds -join ',')"
        "adb_serial=$serial"
        "adb_devices=$((& $adb devices) -join ' | ')"
        "boot_completed=$((& $adb -s $serial shell getprop sys.boot_completed).Trim())"
        "gradle_exit_code=$exitCode"
        "gradle_log=$gradleLog"
        "adb_logcat=$adbLog"
        "emulator_out=$emulatorOut"
        "emulator_err=$emulatorErr"
    ) | Set-Content -Path $summary
}

Assert-Tool $adb "adb"
Assert-Tool $emulator "emulator"

$availableAvds = Get-AvailableAvds
if ($availableAvds -notcontains $AvdName) {
    throw "El AVD '$AvdName' no está disponible. AVDs detectados: $($availableAvds -join ', ')"
}

$serial = $null
$exitCode = 1

try {
    Stop-PreviousEmulators
    Restart-Adb
    Start-Emulator
    $serial = Wait-ForBoot
    Start-Logcat -serial $serial
    Wait-ForPostBootStabilization -serial $serial
    $exitCode = Invoke-ConnectedTests
    Write-Summary -serial $serial -exitCode $exitCode -availableAvds $availableAvds
    exit $exitCode
} finally {
    if (-not $KeepEmulatorOpen) {
        if ($serial) {
            & $adb -s $serial emu kill | Out-Null
        }
        Get-Process emulator, qemu-system-x86_64 -ErrorAction SilentlyContinue | Stop-Process -Force
    }
}
