package bucket

import (
	"log"
	"math"
	"sort"
	"time"

	"sdmm/dm/dmmap"
	"sdmm/util"
)

// Bucket contains data needed to render the map.
// The Bucket itself is made of Chunks.
type Bucket struct {
	// Updating stores the value, if the bucket is updating its chunks.
	Updating bool

	// Chunks is a slice of all chunks in the bucket.
	Chunks []*Chunk
	// Layers stores all available layers for the bucket.
	Layers []float32
	// ChunksByLayers is a map which helps to find chunks with units on the specific layer.
	ChunksByLayers map[float32][]*Chunk
}

func New() *Bucket {
	return &Bucket{}
}

func (b *Bucket) Update(dmm *dmmap.Dmm) {
	b.UpdateV(dmm, nil)
}

// UpdateV will update the current Bucket chunks.
// During a very first call for update we will generate chunks to store data to render.
// If Bucket already has Chunks to render, then we will do the update in the background.
// Method receives a slice with coordinates of tiles, which are needed to be updated.
// If the slice is nil, then all chunks will be updated.
func (b *Bucket) UpdateV(dmm *dmmap.Dmm, tilesToUpdate []util.Point) {
	if b.Chunks != nil {
		go b.update(dmm, tilesToUpdate)
	} else {
		b.generateChunks(dmm.MaxX, dmm.MaxY)
		log.Printf("[bucket] generated chunks number for [%s]: [%d]", dmm.Name, len(b.Chunks))

		start := time.Now()
		log.Printf("[bucket] initial bucket update for [%s]...", dmm.Name)
		b.update(dmm, tilesToUpdate)
		log.Printf("[bucket] bucket updated in [%d] ms", time.Since(start).Milliseconds())
	}
}

func (b *Bucket) update(dmm *dmmap.Dmm, tilesToUpdate []util.Point) {
	b.Updating = true

	if tilesToUpdate != nil {
		// Store a slice of updated chunks to avoid multiple updates for the same chunk area.
		var updatedChunks []*Chunk

		// Only update chunks, which area contains updated tiles.
		for _, tile := range tilesToUpdate {
			for _, chunk := range b.Chunks {
				chunkAlreadyUpdated := false
				for _, updatedChunk := range updatedChunks {
					if chunkAlreadyUpdated = chunk == updatedChunk; chunkAlreadyUpdated {
						break
					}
				}

				if !chunkAlreadyUpdated && chunk.MapBounds.Contains(float32(tile.X), float32(tile.Y)) {
					chunk.update(dmm)
					updatedChunks = append(updatedChunks, chunk)
				}
			}
		}
	} else {
		// Update all available chunks.
		for _, chunk := range b.Chunks {
			chunk.update(dmm)
		}
	}

	b.createChunksLayers()

	b.Updating = false
}

// Method collects layers for every unit in every chunk.
func (b *Bucket) createChunksLayers() {
	chunksByLayers := make(map[float32][]*Chunk, len(b.ChunksByLayers))
	for _, chunk := range b.Chunks {
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

	b.Layers = layers
	b.ChunksByLayers = chunksByLayers
}

// generateChunks will split the map into square areas.
// Every area can store a (chunkMaxTileCapacity*chunkMaxTileCapacity) number of tiles.
// During the generation process we don't fill chunks with actual data. So we only create storages to fill them later.
func (b *Bucket) generateChunks(maxX, maxY int) {
	// Preallocate chunks slice, if there is already some chunks in the bucket.
	b.Chunks = make([]*Chunk, 0, len(b.Chunks))

	// Helps to track if there is tiles to create chunks.
	var chunkCreated bool

	generateYAxis := func(x, xRange int) {
		for y := 1; y <= maxY; y++ {
			chunkCreated = false
			// maxCapacity+1 since we iterate from 1.
			if y%(chunkMaxTileCapacity+1) == 0 {
				chunkCreated = true
				b.Chunks = append(b.Chunks, newChunk(chunkBounds(x, y, xRange, chunkMaxTileCapacity)))
			}
		}
		if !chunkCreated {
			chunkCreated = true
			var nextY int
			if len(b.Chunks) != 0 {
				nextY = int(b.Chunks[len(b.Chunks)-1].MapBounds.Y2) + 1
			}
			b.Chunks = append(b.Chunks, newChunk(
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
		if len(b.Chunks) != 0 {
			nextX = int(b.Chunks[len(b.Chunks)-1].MapBounds.X2) + 1
		}
		generateYAxis(maxX, maxX-nextX)
	}
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