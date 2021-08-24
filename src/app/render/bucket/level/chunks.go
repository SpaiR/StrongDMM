package level

import (
	"log"
	"math"

	"sdmm/app/render/bucket/level/chunk"
	"sdmm/util"
)

// generateChunks creates a map with util.Point's as a key, and a chunk.Chunk's as a value.
// Keys contain a bottom-left bound of the value chunk.
// Generation is made by creating square areas. Each area has a limited number of tiles to store.
// Method won't fill chunks with actual data. It's meant to be done in the future.
func generateChunks(maxX, maxY, iconSize int) map[util.Point]*chunk.Chunk {
	chunks := make(map[util.Point]*chunk.Chunk)

	// Helps to track if there is tiles to create chunks.
	var chunkCreated bool

	// When `max/chunk.Size != 0` there will be dangled areas.
	// Size of such areas physically can't be equal to square.
	// Variables below help to find start points for those areas.
	var nextX, nextY int

	generateAxis := func(x, xRange int) {
		nextY = 0 // Reset for every new Y axis.
		for y := 1; y <= maxY; y++ {
			chunkCreated = false
			// maxCapacity+1 since we iterate from 1.
			if y%(chunk.Size+1) == 0 {
				chunkCreated = true
				x1, y1, x2, y2 := chunkBounds(x, y, xRange, chunk.Size)
				c := chunk.New(x1, y1, x2, y2, float32(iconSize))
				nextX = int(c.MapBounds.X2) + 1
				nextY = int(c.MapBounds.Y2) + 1
				chunks[util.Point{X: int(c.MapBounds.X1), Y: int(c.MapBounds.Y1)}] = c
			}
		}
		if !chunkCreated {
			chunkCreated = true
			x1, y1, x2, y2 := chunkBounds(x, maxY, xRange, maxY-nextY)
			c := chunk.New(x1, y1, x2, y2, float32(iconSize))
			chunks[util.Point{X: int(c.MapBounds.X1), Y: int(c.MapBounds.Y1)}] = c
		}
	}

	for x := 1; x <= maxX; x++ {
		chunkCreated = false
		// maxCapacity+1 since we iterate from 1.
		if x%(chunk.Size+1) == 0 {
			generateAxis(x, chunk.Size)
		}
	}
	if !chunkCreated {
		generateAxis(maxX, maxX-nextX)
	}

	log.Printf("[level] generated chunks number: [%d]", len(chunks))

	return chunks
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
