import { readFileSync, writeFileSync } from 'node:fs'

const [catalogPath, regionsPath] = process.argv.slice(2)
const catalog = JSON.parse(readFileSync(catalogPath, 'utf8'))
const source = JSON.parse(readFileSync(regionsPath, 'utf8'))
const coordinateKey = ({ lat, lon }) => `${lat.toFixed(7)},${lon.toFixed(7)}`

function assembleRings(members, code) {
    const segments = members.map(({ geometry }) => {
        if (!geometry?.every(Boolean) || geometry.length < 2) {
            throw new Error(`Missing boundary geometry for ${code}`)
        }
        return geometry
    })
    const endpoints = new Map()
    for (const segment of segments) {
        for (const point of [segment[0], segment.at(-1)]) {
            const key = coordinateKey(point)
            if (!endpoints.has(key)) endpoints.set(key, [])
            endpoints.get(key).push(segment)
        }
    }

    const unused = new Set(segments)
    const rings = []
    while (unused.size > 0) {
        const first = unused.values().next().value
        unused.delete(first)
        const ring = [...first]
        const start = coordinateKey(ring[0])
        let end = coordinateKey(ring.at(-1))
        while (end !== start) {
            const next = endpoints.get(end)?.find(segment => unused.has(segment))
            if (!next) throw new Error(`Unclosed boundary for ${code}`)
            unused.delete(next)
            const path = coordinateKey(next[0]) === end ? next : [...next].reverse()
            ring.push(...path.slice(1))
            end = coordinateKey(ring.at(-1))
        }
        const bounds = { minLat: Infinity, minLon: Infinity, maxLat: -Infinity, maxLon: -Infinity }
        for (const { lat, lon } of ring) {
            bounds.minLat = Math.min(bounds.minLat, lat)
            bounds.maxLat = Math.max(bounds.maxLat, lat)
            bounds.minLon = Math.min(bounds.minLon, lon)
            bounds.maxLon = Math.max(bounds.maxLon, lon)
        }
        rings.push({ ring, bounds })
    }
    return rings
}

function contains({ ring, bounds }, { latitude, longitude }) {
    if (latitude < bounds.minLat || latitude > bounds.maxLat ||
        longitude < bounds.minLon || longitude > bounds.maxLon) return false
    let inside = false
    for (let i = 1; i < ring.length; i++) {
        const a = ring[i - 1]
        const b = ring[i]
        if ((a.lat > latitude) !== (b.lat > latitude) &&
            longitude < a.lon + (latitude - a.lat) * (b.lon - a.lon) / (b.lat - a.lat)) {
            inside = !inside
        }
    }
    return inside
}

const regions = source.elements
    .filter(element => element.type === 'relation' && /^UZ-[A-Z]{2}$/.test(element.tags?.['ISO3166-2']))
    .map(({ tags, members }) => {
        const code = tags['ISO3166-2']
        const outer = members.filter(member => member.type === 'way' && ['outer', ''].includes(member.role))
        const inner = members.filter(member => member.type === 'way' && member.role === 'inner')
        if (!outer.length) throw new Error(`No outer boundary for ${code}`)
        return { code, rings: [...assembleRings(outer, code), ...assembleRings(inner, code)] }
    })
if (regions.length !== 14 || new Set(regions.map(({ code }) => code)).size !== 14) {
    throw new Error('Expected fourteen unique first-level regions of Uzbekistan')
}

const counts = new Map()
let unmatched = 0
for (const mosque of catalog.mosques) {
    const matches = regions.filter(({ rings }) => rings.filter(ring => contains(ring, mosque.coordinates)).length % 2 === 1)
    if (matches.length === 1) {
        const { code } = matches[0]
        mosque.regionCode = code
        counts.set(code, (counts.get(code) ?? 0) + 1)
    } else {
        delete mosque.regionCode
        unmatched++
    }
}
if (counts.size !== regions.length || unmatched > catalog.mosques.length * 0.01) {
    throw new Error(`Region boundaries failed coverage validation: ${unmatched} unassigned`)
}
catalog.regionsUpdatedAt = source.osm3s?.timestamp_osm_base
writeFileSync(catalogPath, JSON.stringify(catalog) + '\n')
console.log(JSON.stringify({ regions: Object.fromEntries(counts), unmatched }))
