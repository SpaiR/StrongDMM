package bucket

import (
	"log"
	"math"
	"sort"

	"sdmm/dm/dmmap"
	"sdmm/util"
)

// Level stores Chunk's data for a specific Z-level of the map.
type Level struct {
	value int

	// Chunks is a slice of all chunks on the level.
	Chunks []*Chunk
	// Layers stores all available layers for the level.
	Layers []float32
	// ChunksByLayers is the map which helps to find chunks with units on the specific layer.
	ChunksByLayers map[float32][]*Chunk
}

func newLevel(dmm *dmmap.Dmm, level int) *Level {
	return &Level{
		value:  level,
		Chunks: generateChunks(dmm.MaxX, dmm.MaxY),
	}
}

func (l *Level) update(dmm *dmmap.Dmm, tilesToUpdate []util.Point) {
	if tilesToUpdate != nil {
		// Store a slice of updated chunks to avoid multiple updates for the same chunk area.
		var updatedChunks []*Chunk

		// Only update chunks, which area contains updated tiles.
		for _, tile := range tilesToUpdate {
			for _, chunk := range l.Chunks {
				chunkAlreadyUpdated := false
				for _, updatedChunk := range updatedChunks {
					if chunkAlreadyUpdated = chunk == updatedChunk; chunkAlreadyUpdated {
						break
					}
				}

				if !chunkAlreadyUpdated && chunk.MapBounds.Contains(float32(tile.X), float32(tile.Y)) {
					chunk.update(dmm, l.value)
					updatedChunks = append(updatedChunks, chunk)
				}
			}
		}
	} else {
		// Update all available chunks.
		for _, chunk := range l.Chunks {
			chunk.update(dmm, l.value)
		}
	}

	l.createChunksLayers()
}

// Method collects layers for every unit in every chunk.
func (l *Level) createChunksLayers() {
	chunksByLayers := make(map[float32][]*Chunk, len(l.ChunksByLayers))
	for _, chunk := range l.Chunks {
		for chunkLayer := range chunk.UnitsByLayers {
			chunksByLayers[chunkLayer] = append(chunksByLayers[chunkLayer], chunk)
		}
	}

	// Sort layers to do a proper rendering later.
	layers := make([]float32, 0, len(chunksByLayers))
	for layer := range chunksByLayers {
		if len(chunksByLayers[layer]) > 0 {
			layers = append(layers, layer)
		}
	}
	sort.Slice(layers, func(i, j int) bool { return layers[i] < layers[j] })

	l.Layers = layers
	l.ChunksByLayers = chunksByLayers
}

// generateChunks will split the map into square areas.
// Every area can store a (chunkMaxTileCapacity*chunkMaxTileCapacity) number of tiles.
// During the generation process we don't fill chunks with actual data. So we only create storages to fill them later.
func generateChunks(maxX, maxY int) []*Chunk {
	// Chunks capacity.
	chunks := make([]*Chunk, 0, chunksCount(maxX, maxY))

	// Helps to track if there is tiles to create chunks.
	var chunkCreated bool

	generateYAxis := func(x, xRange int) {
		for y := 1; y <= maxY; y++ {
			chunkCreated = false
			// maxCapacity+1 since we iterate from 1.
			if y%(chunkMaxTileCapacity+1) == 0 {
				chunkCreated = true
				chunks = append(chunks, newChunk(chunkBounds(x, y, xRange, chunkMaxTileCapacity)))
			}
		}
		if !chunkCreated {
			chunkCreated = true
			var nextY int
			if len(chunks) != 0 {
				nextY = int(chunks[len(chunks)-1].MapBounds.Y2) + 1
			}
			chunks = append(chunks, newChunk(
				chunkBounds(x, maxY, xRange, maxY-nextY)),
			)
		}
	}

	for x := 1; x <= maxX; x++ {
		chunkCreated = false
		// maxCapacity+1 since we iterate from 1.
		if x%(chunkMaxTileCapacity+1) == 0 {
			generateYAxis(x, chunkMaxTileCapacity)
		}
	}
	if !chunkCreated {
		var nextX int
		if len(chunks) != 0 {
			nextX = int(chunks[len(chunks)-1].MapBounds.X2) + 1
		}
		generateYAxis(maxX, maxX-nextX)
	}

	log.Printf("[bucket] generated chunks number: [%d]", len(chunks))

	return chunks
}

func chunksCount(maxX, maxY int) int {
	xAxisCount := math.Ceil(float64(maxX / chunkMaxTileCapacity))
	yAxisCount := math.Ceil(float64(maxY / chunkMaxTileCapacity))
	return int(xAxisCount * yAxisCount)
}

// chunkBounds returns coords of the chunk area.
// x/y are top right points of the chunk.
// xRange/yRange are width/height of the chunk.
func chunkBounds(x, y, xRange, yRange int) (x1, y1, x2, y2 float32) {
	x1 = float32(math.Max(1, float64(x-xRange)))
	y1 = float32(math.Max(1, float64(y-yRange)))
	x2 = float32(x)
	y2 = float32(y)
	return x1, y1, x2, y2
}
