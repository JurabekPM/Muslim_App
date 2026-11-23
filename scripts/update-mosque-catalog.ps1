param(
    [string]$MosquesJson, [string]$CitiesJson, [string]$LocalitiesJson,
    [string]$BuildingsJson, [string]$RegionsJson
)

$ErrorActionPreference = 'Stop'
$area = 'area["ISO3166-1"="UZ"]["admin_level"="2"]->.uz;'

function Read-Overpass([string]$Path, [string]$Query) {
    if ($Path) { return Get-Content -Raw -LiteralPath $Path | ConvertFrom-Json -AsHashtable }
    $body = 'data=' + [uri]::EscapeDataString("[out:json][timeout:120];$area$Query")
    $response = Invoke-WebRequest -Uri 'https://overpass-api.de/api/interpreter' -Method Post -ContentType 'application/x-www-form-urlencoded' -Body $body -TimeoutSec 180
    return $response.Content | ConvertFrom-Json -AsHashtable
}

function Get-SpecificName($Value) {
    if ($Value -and $Value.Trim() -notmatch '^(masjid|jome|jom\W*e?\s*masjid(i)?|juma masjid(i)?|mosque|juma mosque|мечеть|соборная мечеть|масжид|жоме масжиди|мешіт|مسجد)$') {
        return $Value.Trim()
    }
}

function Convert-Place($Element) {
    $tags = $Element.tags
    $center = if ($Element.center) { $Element.center } else { $Element }
    if ($null -eq $center.lat -or $null -eq $center.lon) { return }
    $place = [ordered]@{
        id = "$($Element.type)/$($Element.id)"
        coordinates = [ordered]@{ latitude = [math]::Round($center.lat, 5); longitude = [math]::Round($center.lon, 5) }
    }
    $names = [ordered]@{}
    foreach ($language in @('uz', 'uz-Cyrl', 'ru', 'en', 'kk', 'ky', 'tg', 'tk')) {
        $name = Get-SpecificName $tags["name:$language"]
        if ($name) { $names[$language] = $name }
    }
    $latinName = Get-SpecificName $tags['name:uz-Latn']
    if ($latinName) { $names.uz = $latinName }
    $name = (Get-SpecificName $tags.name) ?? (Get-SpecificName $tags.official_name) ?? (Get-SpecificName $tags.alt_name) ??
        $names.uz ?? $names['uz-Cyrl'] ?? $names.en ?? $names.ru
    if ($name) {
        $place.name = $name
        foreach ($language in @($names.Keys)) {
            if ($names[$language] -ceq $name) { $names.Remove($language) }
        }
    }
    $street = @($tags['addr:street'], $tags['addr:housenumber']) | Where-Object { $_ }
    if ($tags['addr:full']) { $place.address = $tags['addr:full'] }
    elseif ($street) { $place.address = $street -join ' ' }
    $aliases = @($tags.official_name, $tags.alt_name, $tags.loc_name) -split ';' |
        ForEach-Object { Get-SpecificName $_ } | Where-Object { $_ -and $_ -cne $name } | Select-Object -Unique
    if ($aliases) { $place.aliases = @($aliases) }
    if ($names.Count) { $place.names = $names }
    return $place
}

function Test-InFootprint([double]$Latitude, [double]$Longitude, $Geometry) {
    $inside = $false
    $previous = $Geometry[-1]
    foreach ($point in $Geometry) {
        if (($point.lat -gt $Latitude) -ne ($previous.lat -gt $Latitude)) {
            $intersection = $point.lon + ($Latitude - $point.lat) * ($previous.lon - $point.lon) / ($previous.lat - $point.lat)
            if ($Longitude -lt $intersection) { $inside = !$inside }
        }
        $previous = $point
    }
    return $inside
}

$mosqueSource = Read-Overpass $MosquesJson 'nwr["amenity"="place_of_worship"]["religion"="muslim"](area.uz);out center;'
$citySource = Read-Overpass $CitiesJson 'node["place"~"^(city|town)$"]["name"](area.uz);out;'
$localitySource = Read-Overpass $LocalitiesJson 'node["place"~"^(city|town|village|hamlet|suburb|quarter|neighbourhood)$"]["name"](area.uz);out;'
$buildingSource = Read-Overpass $BuildingsJson 'way["building"="mosque"]["name"](area.uz);out geom;'
$boundarySource = Read-Overpass $RegionsJson 'rel["ISO3166-2"~"^UZ-"]["boundary"="administrative"];out geom;'
$verifiedNames = Get-Content -Raw (Join-Path $PSScriptRoot 'mosque-name-sources.json') | ConvertFrom-Json -AsHashtable

$localityCells = @{}
foreach ($node in $localitySource.elements) {
    $cell = "$([math]::Floor($node.lat * 10))/$([math]::Floor($node.lon * 10))"
    if (!$localityCells.ContainsKey($cell)) { $localityCells[$cell] = [System.Collections.Generic.List[object]]::new() }
    $localityCells[$cell].Add($node)
}

function Find-NearbyLocality([double]$Latitude, [double]$Longitude) {
    $latCell = [math]::Floor($Latitude * 10)
    $lonCell = [math]::Floor($Longitude * 10)
    $longitudeFactor = [math]::Cos([math]::PI * $Latitude / 180)
    $nearest = $null
    $bestDistance = [math]::Pow(8 / 111.2, 2)
    foreach ($lat in ($latCell - 1)..($latCell + 1)) {
        foreach ($lon in ($lonCell - 2)..($lonCell + 2)) {
            foreach ($candidate in $localityCells["$lat/$lon"]) {
                $dy = $candidate.lat - $Latitude
                $dx = ($candidate.lon - $Longitude) * $longitudeFactor
                $distance = $dx * $dx + $dy * $dy
                if ($distance -lt $bestDistance) {
                    $nearest = $candidate
                    $bestDistance = $distance
                }
            }
        }
    }
    return $nearest
}

if ($localitySource.elements.Count -lt 1000 -or $buildingSource.elements.Count -lt 50) {
    throw 'The OpenStreetMap supplementary data is incomplete.'
}
$namedBuildings = @($buildingSource.elements | Where-Object { $_.geometry.Count -ge 4 -and $_.nodes[0] -eq $_.nodes[-1] -and (Get-SpecificName $_.tags.name) })
$activeWayIds = @{}
foreach ($item in $mosqueSource.elements) {
    if ($item.type -eq 'way' -and $item.tags.access -notin @('private', 'no') -and
        $item.tags.place_of_worship -ne 'musalla') {
        $activeWayIds[$item.id] = $true
    }
}
$namedPlaces = @{}
$restoredNames = 0
$mosques = @(
    foreach ($item in $mosqueSource.elements | Sort-Object { if ($_.type -eq 'node') { 1 } else { 0 } }, id) {
        if ($item.tags.access -in @('private', 'no') -or $item.tags.place_of_worship -eq 'musalla') { continue }
        $place = Convert-Place $item
        if (!$place) { continue }
        $hadName = [bool](Get-SpecificName $item.tags.name)
        $verified = $verifiedNames[$place.id]
        $mismatchedTags = if ($verified) {
            $verified.osmTags.Keys | Where-Object { $item.tags[$_] -cne $verified.osmTags[$_] }
        }
        if ($verified -and !$mismatchedTags) {
            $place.name = $verified.name
            if (!$place.names) { $place.names = [ordered]@{} }
            foreach ($key in $verified.names.Keys) { $place.names[$key] = $verified.names[$key] }
        } elseif (!$place.name -and $item.type -eq 'node') {
            $footprints = @($namedBuildings | Where-Object { Test-InFootprint $item.lat $item.lon $_.geometry })
            if ($footprints.Count -eq 1) {
                if ($activeWayIds.ContainsKey($footprints[0].id)) { continue }
                $place.name = Get-SpecificName $footprints[0].tags.name
            }
        } elseif (!$place.name -and $item.type -eq 'way') {
            $nodes = @($item.nodes | Select-Object -Unique)
            $sameBuildings = @($namedBuildings | Where-Object {
                if (!$activeWayIds.ContainsKey($_.id) -or $_.id -eq $item.id) { return $false }
                $footprintNodes = @($_.nodes | Select-Object -Unique)
                $shared = @($footprintNodes | Where-Object { $_ -in $nodes }).Count
                $shared -ge 3 -and $shared -ge [math]::Min($nodes.Count, $footprintNodes.Count) * 0.8
            } | Select-Object -First 2)
            if ($sameBuildings.Count -eq 1) { continue }
        }
        $locality = $item.tags['addr:neighbourhood'] ?? $item.tags['addr:suburb'] ?? $item.tags['addr:place'] ?? $item.tags['addr:city']
        if (!$locality) {
            $nearby = Find-NearbyLocality $place.coordinates.latitude $place.coordinates.longitude
            if ($nearby) {
                $locality = $nearby.tags.name
                $place.localityApproximate = $true
            }
        }
        if ($locality) {
            $place.locality = $locality
        }
        if ($place.name) {
            $duplicates = $namedPlaces[$place.name]
            $nearbyDuplicate = $duplicates | Where-Object {
                $_ -and [math]::Abs($_.coordinates.latitude - $place.coordinates.latitude) -lt 0.0006 -and
                [math]::Abs($_.coordinates.longitude - $place.coordinates.longitude) -lt 0.0006
            } | Select-Object -First 1
            if ($nearbyDuplicate) { continue }
            $namedPlaces[$place.name] = @($duplicates | Where-Object { $_ }) + @($place)
        }
        if (!$hadName -and $place.name) { $restoredNames++ }
        $place
    }
)
$cities = @($citySource.elements | Sort-Object { $_.tags.name }, id | ForEach-Object { Convert-Place $_ })
if ($mosques.Count -lt 100 -or $cities.Count -lt 20) { throw 'The OpenStreetMap response is incomplete.' }
$catalog = [ordered]@{
    attribution = 'OpenStreetMap contributors, ODbL 1.0, https://www.openstreetmap.org/copyright'
    mosquesUpdatedAt = $mosqueSource.osm3s.timestamp_osm_base
    citiesUpdatedAt = $citySource.osm3s.timestamp_osm_base
    localitiesUpdatedAt = $localitySource.osm3s.timestamp_osm_base
    buildingsUpdatedAt = $buildingSource.osm3s.timestamp_osm_base
    mosques = $mosques
    cities = $cities
}
$outputPath = Join-Path $PSScriptRoot '../app/src/main/assets/uzbekistan_mosques.json'
[void][System.IO.Directory]::CreateDirectory((Split-Path -Parent $outputPath))
$catalogTemp = [IO.Path]::GetTempFileName()
$boundaryTemp = [IO.Path]::GetTempFileName()
try {
    $catalog | ConvertTo-Json -Compress -Depth 10 | Set-Content -LiteralPath $catalogTemp -Encoding utf8NoBOM
    $boundarySource | ConvertTo-Json -Compress -Depth 15 | Set-Content -LiteralPath $boundaryTemp -Encoding utf8NoBOM
    node (Join-Path $PSScriptRoot 'assign-mosque-regions.mjs') $catalogTemp $boundaryTemp
    if ($LASTEXITCODE -ne 0) { throw 'Mosque region assignment failed.' }
    Copy-Item -LiteralPath $catalogTemp -Destination $outputPath
} finally {
    Remove-Item -LiteralPath $catalogTemp, $boundaryTemp
}
Write-Output "$($mosques.Count) mosques and $($cities.Count) cities saved."
$unnamed = @($mosques | Where-Object { !$_.name })
$withContext = @($unnamed | Where-Object { $_.locality -or $_.address })
Write-Output "$restoredNames specific names restored. $($withContext.Count) of $($unnamed.Count) unnamed records have locality or address context."
