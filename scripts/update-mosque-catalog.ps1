param([string]$MosquesJson, [string]$CitiesJson)

$ErrorActionPreference = 'Stop'
$area = 'area["ISO3166-1"="UZ"]["admin_level"="2"]->.uz;'

function Read-Overpass([string]$Path, [string]$Query) {
    if ($Path) { return Get-Content -Raw -LiteralPath $Path | ConvertFrom-Json -AsHashtable }
    $body = 'data=' + [uri]::EscapeDataString("[out:json][timeout:120];$area$Query")
    $response = Invoke-WebRequest -Uri 'https://overpass-api.de/api/interpreter' -Method Post -ContentType 'application/x-www-form-urlencoded' -Body $body -TimeoutSec 180
    return $response.Content | ConvertFrom-Json -AsHashtable
}

function Convert-Place($Element) {
    $tags = $Element.tags
    $center = if ($Element.center) { $Element.center } else { $Element }
    if ($null -eq $center.lat -or $null -eq $center.lon) { return }
    $place = [ordered]@{
        id = "$($Element.type)/$($Element.id)"
        coordinates = [ordered]@{ latitude = [math]::Round($center.lat, 5); longitude = [math]::Round($center.lon, 5) }
    }
    if ($tags.name) { $place.name = $tags.name }
    $street = @($tags['addr:street'], $tags['addr:housenumber']) | Where-Object { $_ }
    if ($street) { $place.address = $street -join ' ' }
    $names = [ordered]@{}
    foreach ($language in @('uz', 'uz-Cyrl', 'ru', 'en', 'kk', 'ky', 'tg', 'tk')) {
        $name = $tags["name:$language"]
        if ($name -and $name -ne $tags.name) { $names[$language] = $name }
    }
    if ($names.Count) { $place.names = $names }
    return $place
}

$mosqueSource = Read-Overpass $MosquesJson 'nwr["amenity"="place_of_worship"]["religion"="muslim"](area.uz);out center;'
$citySource = Read-Overpass $CitiesJson 'node["place"~"^(city|town)$"]["name"](area.uz);out;'
$namedPlaces = @{}
$mosques = @(
    foreach ($item in $mosqueSource.elements | Sort-Object { if ($_.type -eq 'node') { 1 } else { 0 } }, id) {
        if ($item.tags.access -in @('private', 'no') -or $item.tags.place_of_worship -eq 'musalla') { continue }
        $place = Convert-Place $item
        if (!$place) { continue }
        if ($place.name) {
            $duplicates = $namedPlaces[$place.name]
            $nearbyDuplicate = $duplicates | Where-Object {
                $_ -and [math]::Abs($_.coordinates.latitude - $place.coordinates.latitude) -lt 0.0006 -and
                [math]::Abs($_.coordinates.longitude - $place.coordinates.longitude) -lt 0.0006
            } | Select-Object -First 1
            if ($nearbyDuplicate) { continue }
            $namedPlaces[$place.name] = @($duplicates | Where-Object { $_ }) + @($place)
        }
        $place
    }
)
$cities = @($citySource.elements | Sort-Object { $_.tags.name }, id | ForEach-Object { Convert-Place $_ })
if ($mosques.Count -lt 100 -or $cities.Count -lt 20) { throw 'The OpenStreetMap response is incomplete.' }
$catalog = [ordered]@{
    attribution = 'OpenStreetMap contributors, ODbL 1.0, https://www.openstreetmap.org/copyright'
    mosquesUpdatedAt = $mosqueSource.osm3s.timestamp_osm_base
    citiesUpdatedAt = $citySource.osm3s.timestamp_osm_base
    mosques = $mosques
    cities = $cities
}
$outputPath = Join-Path $PSScriptRoot '../app/src/main/assets/uzbekistan_mosques.json'
[void][System.IO.Directory]::CreateDirectory((Split-Path -Parent $outputPath))
$catalog | ConvertTo-Json -Compress -Depth 10 | Set-Content -LiteralPath $outputPath -Encoding utf8NoBOM
Write-Output "$($mosques.Count) mosques and $($cities.Count) cities saved."
