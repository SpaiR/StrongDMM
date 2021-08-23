package level

import (
	"log"
	"math"

	"sdmm/app/render/bucket/level/chunk"
)

// generateChunks creates a slice filled with Chunk's.
// Generation is made by creating square areas. Each area will have a limited number of tiles to store.
// Method won't fill chunks with actual data. It's meant to be done in the future.
func generateChunks(maxX, maxY int) []*chunk.Chunk {
	// Chunks capacity.
	chunks := make([]*chunk.Chunk, 0, totalChunksCount(maxX, maxY))

	// Helps to track if there is tiles to create chunks.
	var chunkCreated bool

	generateAxis := func(x, xRange int) {
		for y := 1; y <= maxY; y++ {
			chunkCreated = false
			// maxCapacity+1 since we iterate from 1.
			if y%(chunk.Size+1) == 0 {
				chunkCreated = true
				chunks = append(chunks, chunk.New(chunkBounds(x, y, xRange, chunk.Size)))
			}
		}
		if !chunkCreated {
			chunkCreated = true
			var nextY int
			if len(chunks) != 0 {
				nextY = int(chunks[len(chunks)-1].MapBounds.Y2) + 1
			}
			chunks = append(chunks, chunk.New(chunkBounds(x, maxY, xRange, maxY-nextY)))
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
		var nextX int
		if len(chunks) != 0 {
			nextX = int(chunks[len(chunks)-1].MapBounds.X2) + 1
		}
		generateAxis(maxX, maxX-nextX)
	}

	log.Printf("[level] generated chunks number: [%d]", len(chunks))

	return chunks
}

func totalChunksCount(maxX, maxY int) int {
	xAxisCount := math.Ceil(float64(maxX / chunk.Size))
	yAxisCount := math.Ceil(float64(maxY / chunk.Size))
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
