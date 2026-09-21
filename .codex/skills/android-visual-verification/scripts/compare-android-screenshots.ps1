[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$ReferencePath,
    [Parameter(Mandatory = $true)][string]$ActualPath,
    [Parameter(Mandatory = $true)][string]$ReportPath,
    [Parameter(Mandatory = $true)][string]$DiffPath,
    [string]$OverlayPath,
    [string]$SideBySidePath,
    [double]$MaxChangedPixelRatio = 0.02,
    [int]$PixelDeltaThreshold = 16
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

function Load-Bitmap([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path)) { throw "Image not found: $Path" }
    return [System.Drawing.Bitmap]::new((Resolve-Path -LiteralPath $Path).Path)
}

$reference = Load-Bitmap $ReferencePath
$actual = Load-Bitmap $ActualPath
$reportDirectory = Split-Path -Parent $ReportPath
$diffDirectory = Split-Path -Parent $DiffPath
New-Item -ItemType Directory -Force -Path $reportDirectory, $diffDirectory | Out-Null

$sameSize = $reference.Width -eq $actual.Width -and $reference.Height -eq $actual.Height
$width = [Math]::Min($reference.Width, $actual.Width)
$height = [Math]::Min($reference.Height, $actual.Height)
$changed = 0
$total = [Math]::Max(1, $width * $height)
$diff = [System.Drawing.Bitmap]::new($width, $height)
for ($y = 0; $y -lt $height; $y++) {
    for ($x = 0; $x -lt $width; $x++) {
        $referencePixel = $reference.GetPixel($x, $y)
        $actualPixel = $actual.GetPixel($x, $y)
        $delta = [Math]::Max([Math]::Abs($referencePixel.R - $actualPixel.R), [Math]::Max([Math]::Abs($referencePixel.G - $actualPixel.G), [Math]::Abs($referencePixel.B - $actualPixel.B)))
        if ($delta -gt $PixelDeltaThreshold) {
            $changed++
            $diff.SetPixel($x, $y, [System.Drawing.Color]::Red)
        } else {
            $diff.SetPixel($x, $y, [System.Drawing.Color]::Transparent)
        }
    }
}
$ratio = $changed / $total
$diagnosticStatus = if ($sameSize -and $ratio -le $MaxChangedPixelRatio) { 'MATCH' } else { 'DIFFERENT' }
$diff.Save($DiffPath, [System.Drawing.Imaging.ImageFormat]::Png)
$overlaySaved = $null
if ($OverlayPath -and $sameSize) {
    $overlayDirectory = Split-Path -Parent $OverlayPath
    New-Item -ItemType Directory -Force -Path $overlayDirectory | Out-Null
    $overlay = [System.Drawing.Bitmap]::new($reference.Width, $reference.Height)
    for ($y = 0; $y -lt $reference.Height; $y++) {
        for ($x = 0; $x -lt $reference.Width; $x++) {
            $referencePixel = $reference.GetPixel($x, $y)
            $actualPixel = $actual.GetPixel($x, $y)
            $overlay.SetPixel($x, $y, [System.Drawing.Color]::FromArgb(
                255,
                [int](($referencePixel.R + $actualPixel.R) / 2),
                [int](($referencePixel.G + $actualPixel.G) / 2),
                [int](($referencePixel.B + $actualPixel.B) / 2)))
        }
    }
    $overlay.Save($OverlayPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $overlay.Dispose()
    $overlaySaved = (Resolve-Path -LiteralPath $OverlayPath).Path
}
$sideBySideSaved = $null
if ($SideBySidePath) {
    $sideDirectory = Split-Path -Parent $SideBySidePath
    New-Item -ItemType Directory -Force -Path $sideDirectory | Out-Null
    $separatorWidth = 4
    $sideBySide = [System.Drawing.Bitmap]::new($reference.Width + $separatorWidth + $actual.Width, [Math]::Max($reference.Height, $actual.Height))
    $graphics = [System.Drawing.Graphics]::FromImage($sideBySide)
    $graphics.Clear([System.Drawing.Color]::White)
    $graphics.DrawImage($reference, 0, 0)
    $graphics.FillRectangle([System.Drawing.Brushes]::DimGray, $reference.Width, 0, $separatorWidth, $sideBySide.Height)
    $graphics.DrawImage($actual, $reference.Width + $separatorWidth, 0)
    $graphics.Dispose()
    $sideBySide.Save($SideBySidePath, [System.Drawing.Imaging.ImageFormat]::Png)
    $sideBySide.Dispose()
    $sideBySideSaved = (Resolve-Path -LiteralPath $SideBySidePath).Path
}
$lines = @(
    "diagnostic_status: $diagnosticStatus",
    "reference: $((Resolve-Path -LiteralPath $ReferencePath).Path)",
    "actual: $((Resolve-Path -LiteralPath $ActualPath).Path)",
    "reference_size: $($reference.Width)x$($reference.Height)",
    "actual_size: $($actual.Width)x$($actual.Height)",
    "same_size: $sameSize",
    "changed_pixels: $changed/$total",
    ("changed_pixel_ratio: {0:P4}" -f $ratio),
    "pixel_delta_threshold: $PixelDeltaThreshold",
    "max_changed_pixel_ratio: $MaxChangedPixelRatio",
    "diff: $((Resolve-Path -LiteralPath $DiffPath).Path)",
    "overlay: $overlaySaved",
    "side_by_side: $sideBySideSaved"
)
$lines | Set-Content -LiteralPath $ReportPath -Encoding UTF8
$reference.Dispose(); $actual.Dispose(); $diff.Dispose()
$lines | Write-Output
