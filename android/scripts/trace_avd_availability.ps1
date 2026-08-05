param(
    [ValidateSet("baseline", "list-avds", "restart-adb", "manual-start", "emulator-close", "cert-script")]
    [string]$Action,
    [string]$AvdName = "Medium_Phone",
    [int]$ObserveSeconds = 20,
    [switch]$KeepEmulatorOpen
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$artifactsRoot = Join-Path $repoRoot ".artifacts\avd-trace"
$runId = Get-Date -Format "yyyyMMdd-HHmmss"
$runDir = Join-Path $artifactsRoot "$runId-$Action"

$sdkRoot = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$adb = Join-Path $sdkRoot "platform-tools\adb.exe"
$emulator = Join-Path $sdkRoot "emulator\emulator.exe"
$avdHome = Join-Path $env:USERPROFILE ".android\avd"
$iniPath = Join-Path $avdHome "$AvdName.ini"
$configPath = Join-Path (Join-Path $avdHome "$AvdName.avd") "config.ini"

$snapshotBefore = Join-Path $runDir "before.txt"
$snapshotAfter = Join-Path $runDir "after.txt"
$watchLog = Join-Path $runDir "watch-events.txt"
$stdoutLog = Join-Path $runDir "action-stdout.txt"
$stderrLog = Join-Path $runDir "action-stderr.txt"
$summaryLog = Join-Path $runDir "summary.txt"

New-Item -ItemType Directory -Force -Path $runDir | Out-Null

function Assert-Tool([string]$path, [string]$label) {
    if (-not (Test-Path $path)) {
        throw "$label no encontrado en $path"
    }
}

function Get-EnvValue([string]$name) {
    $value = [Environment]::GetEnvironmentVariable($name)
    if ($null -eq $value) {
        return ""
    }
    return $value
}

function Get-TrackedProcesses {
    $names = @("adb.exe", "emulator.exe", "qemu-system-x86_64.exe", "java.exe", "studio64.exe", "studio.exe")
    $filter = ($names | ForEach-Object { "Name='$_'" }) -join " OR "
    Get-CimInstance Win32_Process -Filter $filter |
        Select-Object Name, ProcessId, ParentProcessId, ExecutablePath, CommandLine
}

function Write-Snapshot([string]$path, [string]$label) {
    $emuList = @(& $emulator -list-avds 2>&1)
    $adbDevices = @(& $adb devices -l 2>&1)
    $avdExists = Test-Path $avdHome
    $iniExists = Test-Path $iniPath
    $configExists = Test-Path $configPath
    $avdListing = if ($avdExists) {
        Get-ChildItem $avdHome -Force -Recurse -ErrorAction SilentlyContinue |
            Select-Object FullName, Length, LastWriteTime |
            Out-String
    } else {
        "AVD home no existe."
    }
    $tracked = Get-TrackedProcesses | Format-Table -AutoSize | Out-String

    @(
        "label=$label"
        "timestamp=$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
        "cwd=$((Get-Location).Path)"
        "HOME=$(Get-EnvValue 'HOME')"
        "USERPROFILE=$(Get-EnvValue 'USERPROFILE')"
        "ANDROID_AVD_HOME=$(Get-EnvValue 'ANDROID_AVD_HOME')"
        "ANDROID_SDK_HOME=$(Get-EnvValue 'ANDROID_SDK_HOME')"
        "ANDROID_HOME=$(Get-EnvValue 'ANDROID_HOME')"
        "ANDROID_SDK_ROOT=$(Get-EnvValue 'ANDROID_SDK_ROOT')"
        "avd_home=$avdHome"
        "ini_path=$iniPath"
        "config_path=$configPath"
        "ini_exists=$iniExists"
        "config_exists=$configExists"
        "emulator_list_avds=$($emuList -join ' | ')"
        "adb_devices=$($adbDevices -join ' | ')"
        "avd_listing_begin"
        $avdListing.TrimEnd()
        "avd_listing_end"
        "tracked_processes_begin"
        $tracked.TrimEnd()
        "tracked_processes_end"
    ) | Set-Content -Path $path
}

function Start-AvdWatcher {
    if (-not (Test-Path $avdHome)) {
        New-Item -ItemType Directory -Force -Path $avdHome | Out-Null
    }

    $watcher = New-Object System.IO.FileSystemWatcher
    $watcher.Path = $avdHome
    $watcher.IncludeSubdirectories = $true
    $watcher.EnableRaisingEvents = $true
    $watcher.NotifyFilter = [System.IO.NotifyFilters]"FileName, DirectoryName, LastWrite, CreationTime, Size"

    $action = {
        $line = "{0} {1} {2} old={3}" -f `
            (Get-Date -Format "yyyy-MM-dd HH:mm:ss.fff"), `
            $Event.SourceEventArgs.ChangeType, `
            $Event.SourceEventArgs.FullPath, `
            ($(if ($Event.SourceEventArgs -is [System.IO.RenamedEventArgs]) { $Event.SourceEventArgs.OldFullPath } else { "" }))
        Add-Content -Path $using:watchLog -Value $line
    }

    $created = Register-ObjectEvent -InputObject $watcher -EventName Created -Action $action
    $changed = Register-ObjectEvent -InputObject $watcher -EventName Changed -Action $action
    $deleted = Register-ObjectEvent -InputObject $watcher -EventName Deleted -Action $action
    $renamed = Register-ObjectEvent -InputObject $watcher -EventName Renamed -Action $action

    return [pscustomobject]@{
        Watcher = $watcher
        Registrations = @($created, $changed, $deleted, $renamed)
    }
}

function Stop-AvdWatcher($state) {
    foreach ($registration in $state.Registrations) {
        Unregister-Event -SourceIdentifier $registration.Name -ErrorAction SilentlyContinue
        Remove-Job -Id $registration.Id -Force -ErrorAction SilentlyContinue
    }
    $state.Watcher.EnableRaisingEvents = $false
    $state.Watcher.Dispose()
}

function Restart-Adb {
    & $adb kill-server | Out-Null
    & $adb start-server | Out-Null
}

function Stop-Emulators {
    & $adb devices | Out-Null
    $serials = & $adb devices | Select-String "^emulator-" | ForEach-Object { ($_ -split "\s+")[0] }
    foreach ($serial in $serials) {
        & $adb -s $serial emu kill 2>$null | Out-Null
    }
    Get-Process emulator, qemu-system-x86_64 -ErrorAction SilentlyContinue | Stop-Process -Force
}

function Invoke-Action {
    switch ($Action) {
        "baseline" {
            "baseline-noop" | Set-Content -Path $stdoutLog
        }
        "list-avds" {
            & $emulator -list-avds 1>$stdoutLog 2>$stderrLog
        }
        "restart-adb" {
            & $adb kill-server 1>$stdoutLog 2>$stderrLog
            & $adb start-server 1>>$stdoutLog 2>>$stderrLog
        }
        "manual-start" {
            Stop-Emulators
            Restart-Adb
            $env:ANDROID_AVD_HOME = $avdHome
            $env:ANDROID_SDK_HOME = $env:USERPROFILE
            $process = Start-Process -FilePath $emulator `
                -ArgumentList "-avd",$AvdName,"-wipe-data","-no-snapshot-load","-no-snapshot-save","-no-boot-anim","-gpu","swiftshader_indirect","-no-audio","-verbose" `
                -RedirectStandardOutput $stdoutLog `
                -RedirectStandardError $stderrLog `
                -PassThru `
                -WindowStyle Hidden
            Start-Sleep -Seconds $ObserveSeconds
            if (-not $KeepEmulatorOpen) {
                & $adb -s emulator-5554 emu kill 2>$null | Out-Null
                Start-Sleep -Seconds 3
                Get-Process -Id $process.Id -ErrorAction SilentlyContinue | Stop-Process -Force
            }
        }
        "emulator-close" {
            Stop-Emulators
            "emulators-stopped" | Set-Content -Path $stdoutLog
        }
        "cert-script" {
            & (Join-Path $PSScriptRoot "run_projects_connected_cert.ps1") -AvdName $AvdName -BootTimeoutSeconds $ObserveSeconds 1>$stdoutLog 2>$stderrLog
        }
    }
}

Assert-Tool $adb "adb"
Assert-Tool $emulator "emulator"

$env:ANDROID_AVD_HOME = $avdHome
$env:ANDROID_SDK_HOME = $env:USERPROFILE

Write-Snapshot -path $snapshotBefore -label "before-$Action"
$watcherState = Start-AvdWatcher
$actionFailed = $false
$errorMessage = ""

try {
    Invoke-Action
} catch {
    $actionFailed = $true
    $errorMessage = $_.Exception.Message
    Add-Content -Path $stderrLog -Value $_
} finally {
    Start-Sleep -Seconds 2
    Stop-AvdWatcher -state $watcherState
    Write-Snapshot -path $snapshotAfter -label "after-$Action"
}

@(
    "run_dir=$runDir"
    "action=$Action"
    "avd_name=$AvdName"
    "observe_seconds=$ObserveSeconds"
    "action_failed=$actionFailed"
    "error_message=$errorMessage"
    "before_snapshot=$snapshotBefore"
    "after_snapshot=$snapshotAfter"
    "watch_log=$watchLog"
    "stdout_log=$stdoutLog"
    "stderr_log=$stderrLog"
) | Set-Content -Path $summaryLog

if ($actionFailed) {
    throw $errorMessage
}
