# Hybrid Bridging

A client-side Fabric mod for Minecraft Java 1.21.1 that preserves normal vanilla block placement and adds a validated fallback search when vanilla placement misses.

## Features

- Vanilla placement remains untouched whenever the incoming interaction result is a block hit.
- Fallback placement runs only on a vanilla miss.
- Edge assistance checks for missing floor support below the block ahead.
- Diagonal assistance ranks candidates using actual horizontal player movement.
- Candidate validation checks solid support faces, replaceable destinations, player collision, Minecraft placement context, and block placement state.
- Candidate face hit points are measured against the actual crosshair ray segment.
- Uses the player's current block interaction range and does not increase reach.
- Does not modify movement, input, or physics.
- Client-side only. No server installation required.

## How It Works

When vanilla returns a block hit, Hybrid Bridging does nothing. On a miss, it searches nearby support blocks. Each candidate must have a solid face, a replaceable destination, avoid the player's bounding box, pass ItemPlacementContext validation, produce a valid block placement state, remain within interaction range, and lie within the configured tolerance of the crosshair ray segment.

The returned hit result identifies the support block and the face being clicked. The server remains authoritative and may reject placements that are invalid for the current world or game mode.

## Configuration

The mod creates config/bridging-mod.json on first launch.

| Option | Default | Description |
|---|---:|---|
| enabled | true | Master toggle. |
| fallbackSearchRadius | 3 | Search radius, clamped from 1 to 5. |
| placementTolerance | 0.25 | Maximum distance from the crosshair ray segment, clamped from 0.01 to 1.5 blocks. |
| edgeAssistance | true | Prefers forward placements when floor support is missing ahead. |
| diagonalAssistance | true | Adds movement-alignment preference during diagonal movement. |

Malformed configuration falls back to defaults. Numeric values are sanitized when loaded.

## Requirements

- Minecraft Java Edition 1.21.1
- Fabric Loader 0.16.10+
- Fabric API
- Java 21

## Building

Clone the repository and run ./gradlew build (or gradlew.bat build on Windows). The compiled JAR is generated in build/libs/.

## Tests

Run ./gradlew test (or gradlew.bat test on Windows).

## License

See LICENSE for licensing information.
