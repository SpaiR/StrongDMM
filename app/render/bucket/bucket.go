package bucket

import (
	"log"
	"math"
	"time"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
)

// Bucket Contains data needed to render the map.
// The Bucket itself is made of Chunks.
type Bucket struct {
	Updating bool // When true, then the Bucket updates its data at the moment.
	Chunks   []*Chunk
}

func New() *Bucket {
	return &Bucket{}
}

// Update will update the current Bucket chunks.
// During a very first call for update we will generate chunks to store data to render.
// If Bucket already has Chunks to render, then we will do the update in the background.
func (b *Bucket) Update(dmm *dmmap.Dmm) {
	if b.Chunks != nil {
		go b.update(dmm)
	} else {
		b.generateChunks(dmm.MaxX, dmm.MaxY)
		log.Printf("[bucket] generated chunks number for [%s]: [%d]", dmm.Name, len(b.Chunks))

		start := time.Now()
		log.Printf("[bucket] initial bucket update for [%s]...", dmm.Name)
		b.update(dmm)
		log.Printf("[bucket] bucket updated in [%d] ms", time.Since(start).Milliseconds())
	}
}

func (b *Bucket) update(dmm *dmmap.Dmm) {
	b.Updating = true
	for _, chunk := range b.Chunks {
		chunk.update(dmm)
	}
	b.Updating = false
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
			if y%chunkMaxTileCapacity == 0 {
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
		if x%chunkMaxTileCapacity == 0 {
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
