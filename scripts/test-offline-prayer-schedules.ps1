param(
    [Parameter(Mandatory)]
    [ValidatePattern('^emulator-[0-9]+$')]
    [string]$Serial,
    [string]$Adb = 'adb'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$app = 'uz.turgunboyevjurabek.muslimapp'
$runner = "$app.test/androidx.test.runner.AndroidJUnitRunner"
$testClass = "$app.OfflinePrayerScheduleTest"

function Invoke-EmulatorAdb {
    & $Adb -s $Serial @args
    if ($LASTEXITCODE -ne 0) { throw "ADB failed on $Serial for $($args -join ' ')" }
}

function Restore-Emulator {
    try { Invoke-EmulatorAdb @args | Out-Null }
    catch { Write-Warning $_.Exception.Message }
}

function Invoke-ScheduleTest([string]$Method) {
    $result = Invoke-EmulatorAdb shell am instrument -w -e offlineScheduleTest true `
        -e offlineScheduleEpoch $epoch -e class "$testClass#$Method" $runner
    $result | Write-Output
    if (($result -join "`n") -notmatch '(?m)^OK \(1 test\)\s*$') {
        throw "Offline schedule test failed at $Method"
    }
}

Invoke-EmulatorAdb emu avd name | Out-Null
if (Invoke-EmulatorAdb shell pm list packages $app) {
    throw 'Use an emulator without Muslim App installed to protect existing app data.'
}

$debugApk = Join-Path $root 'app/build/outputs/apk/debug/app-debug.apk'
$testApk = Join-Path $root 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
if (-not (Test-Path -LiteralPath $debugApk) -or -not (Test-Path -LiteralPath $testApk)) {
    throw 'Build :app:assembleDebug and :app:assembleDebugAndroidTest before running this test.'
}

$airplane = if ((Invoke-EmulatorAdb shell settings get global airplane_mode_on) -eq '1') { 'enable' } else { 'disable' }
$wifi = if ((Invoke-EmulatorAdb shell cmd wifi status) -match 'Wifi is enabled') { 'enable' } else { 'disable' }
$location = Invoke-EmulatorAdb shell cmd location is-location-enabled
$epoch = Invoke-EmulatorAdb shell date +%s
$installedApp = $false
$installedTest = $false
$networkChanged = $false
$locationChanged = $false

try {
    Invoke-EmulatorAdb install $debugApk
    $installedApp = $true
    Invoke-EmulatorAdb install -t $testApk
    $installedTest = $true
    Invoke-ScheduleTest seedSchedules

    $networkChanged = $true
    Invoke-EmulatorAdb shell cmd connectivity airplane-mode enable
    Invoke-EmulatorAdb shell svc wifi disable
    if ($location -eq 'false') {
        $locationChanged = $true
        Invoke-EmulatorAdb shell cmd location set-location-enabled true
    }
    Invoke-EmulatorAdb shell input keyevent KEYCODE_WAKEUP
    Invoke-EmulatorAdb shell wm dismiss-keyguard

    # Connectivity changes are asynchronous. Verify the emulator before launching the Activity.
    $offline = $false
    for ($attempt = 0; $attempt -lt 20; $attempt++) {
        if ((Invoke-EmulatorAdb shell dumpsys connectivity) -match '^\s*Active default network: none\s*$') {
            $offline = $true
            break
        }
        Start-Sleep -Milliseconds 500
    }
    if (-not $offline) { throw 'The emulator still has an active network.' }

    Invoke-EmulatorAdb shell am start -W -n "$app/.MainActivity"
    if (-not (Invoke-EmulatorAdb shell "pidof $app || true")) { throw 'The app did not start.' }
    Invoke-EmulatorAdb shell am force-stop $app
    if (Invoke-EmulatorAdb shell "pidof $app || true") { throw 'The app process is still running.' }

    Write-Output 'App process stopped. Emulator has no active default network.'
    Invoke-ScheduleTest showsCachedSchedulesAfterOfflineRestart
} finally {
    if ($installedTest) { Restore-Emulator uninstall "$app.test" }
    if ($installedApp) { Restore-Emulator uninstall $app }
    if ($locationChanged) { Restore-Emulator shell cmd location set-location-enabled false }
    if ($networkChanged) {
        Restore-Emulator shell cmd connectivity airplane-mode $airplane
        Restore-Emulator shell svc wifi $wifi
    }
}
