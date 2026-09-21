[CmdletBinding()]
param(
    [string]$ProjectRoot,
    [string]$DeviceId,
    [string]$ScreenshotPath,
    [switch]$SkipBuild,
    [int]$LaunchWaitSeconds = 15
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$skillRoot = Split-Path -Parent $PSScriptRoot
$defaultProjectRoot = (Resolve-Path (Join-Path $skillRoot '..\..\..')).Path
if (-not $ProjectRoot) { $ProjectRoot = $defaultProjectRoot }
if (-not $ScreenshotPath) { $ScreenshotPath = Join-Path $ProjectRoot 'artifacts\android-visual\latest.png' }
function Invoke-Checked { param([string]$FilePath, [string[]]$Arguments); & $FilePath @Arguments; if ($LASTEXITCODE -ne 0) { throw "$FilePath exited with code $LASTEXITCODE" } }
function Find-Executable { param([string]$CommandName, [string[]]$Candidates); foreach ($candidate in $Candidates) { if ($candidate -and (Test-Path $candidate)) { return $candidate } }; $command = Get-Command $CommandName -ErrorAction SilentlyContinue; if ($command) { return $command.Source }; throw "Could not find $CommandName." }
Set-Location $ProjectRoot
$flutter = Find-Executable 'flutter' @('C:\Users\Dominik\Downloads\FlutterSDK\flutter\bin\flutter.bat', 'C:\Users\Dominik\Downloads\FlutterSDK\flutter\bin\flutter.exe')
$adb = Find-Executable 'adb' @('C:\Users\Dominik\AppData\Local\Android\Sdk\platform-tools\adb.exe')
if (-not $DeviceId) {
    $readyEmulator = (& $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '^\s*emulator-\S+\s+device\s*$' } | Select-Object -First 1)
    if (-not $readyEmulator) { throw 'No ready Android emulator found. Run adb devices and start an emulator.' }
    $DeviceId = ($readyEmulator -split '\s+')[0]
}
& $adb -s $DeviceId get-state 2>$null
if ($LASTEXITCODE -ne 0) { throw "Android device '$DeviceId' is not available." }
$gradleFile = Join-Path $ProjectRoot 'android\app\build.gradle.kts'
$manifestFile = Join-Path $ProjectRoot 'android\app\src\main\AndroidManifest.xml'
$applicationId = $null
if (Test-Path $gradleFile) { $match = Select-String -Path $gradleFile -Pattern 'applicationId\s*=\s*"([^"]+)"' | Select-Object -First 1; if ($match) { $applicationId = $match.Matches[0].Groups[1].Value } }
if (-not $applicationId -and (Test-Path $manifestFile)) { $match = Select-String -Path $manifestFile -Pattern 'package="([^"]+)"' | Select-Object -First 1; if ($match) { $applicationId = $match.Matches[0].Groups[1].Value } }
if (-not $applicationId) { throw 'Could not discover the Android application id.' }
if (-not $SkipBuild) { Invoke-Checked $flutter @('build', 'apk', '--debug') }
$apk = Join-Path $ProjectRoot 'build\app\outputs\flutter-apk\app-debug.apk'
if (-not (Test-Path $apk)) { throw "Debug APK not found at $apk" }
Invoke-Checked $adb @('-s', $DeviceId, 'install', '-r', $apk)
Invoke-Checked $adb @('-s', $DeviceId, 'shell', 'monkey', '-p', $applicationId, '1')
if ($LaunchWaitSeconds -lt 0) { throw 'LaunchWaitSeconds must be zero or greater.' }
Start-Sleep -Seconds $LaunchWaitSeconds
$screenshotDirectory = Split-Path -Parent $ScreenshotPath
New-Item -ItemType Directory -Force -Path $screenshotDirectory | Out-Null
& $adb -s $DeviceId exec-out screencap -p > $ScreenshotPath
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $ScreenshotPath)) { throw "Could not capture screenshot at $ScreenshotPath" }
$processId = (& $adb -s $DeviceId shell pidof $applicationId).Trim()
if (-not $processId) { throw "Jokerboard process '$applicationId' is not running after launch." }
$focus = & $adb -s $DeviceId shell dumpsys window | Select-String 'mCurrentFocus|mFocusedApp'
$focusText = ($focus -join ' ')
$targetFocused = $focusText -match [regex]::Escape($applicationId)
$result = [pscustomobject]@{ DeviceId = $DeviceId; ApplicationId = $applicationId; ProcessId = $processId; Apk = $apk; Screenshot = (Resolve-Path $ScreenshotPath).Path; Focus = $focusText; TargetFocused = $targetFocused }
$result
if (-not $targetFocused) { throw "Target application '$applicationId' is not focused. Treat this evidence as not verified." }
