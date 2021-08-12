package bucket

import (
	"log"

	"sdmm/dm/dmmap"
)

// Maximum number of tiles by any axis which could be stored in the Chunk.
const chunkMaxTileCapacity = 25

// Chunk stores the actual data to render.
// It stores two types of bounds: view and map.
// View bounds are a visual bounds which are used to ignore the chunk if it's out of the user viewport.
// Map bounds are coordinate points of tiles in the chunk.
type Chunk struct {
	ViewBounds, MapBounds Bounds

	UnitsByLayers map[float32][]unit
}

func newChunk(x1, y1, x2, y2 float32) *Chunk {
	return &Chunk{
		ViewBounds: Bounds{(x1 - 1) * 32, (y1 - 1) * 32, x2 * 32, y2 * 32},
		MapBounds:  Bounds{x1, y1, x2, y2},
	}
}

func (c *Chunk) update(dmm *dmmap.Dmm) {
	// Create A storage for our units by Layers with initial capacity.
	// Inner slices are created with initial capacity as well.
	unitsByLayers := make(map[float32][]unit, len(c.UnitsByLayers))
	for layer := range c.UnitsByLayers {
		unitsByLayers[layer] = make([]unit, 0, len(c.UnitsByLayers[layer]))
	}

	for x := c.MapBounds.X1; x <= c.MapBounds.X2; x++ {
		for y := c.MapBounds.Y1; y <= c.MapBounds.Y2; y++ {
			x, y := int(x), int(y)
			for _, i := range dmm.GetTile(x, y, 1).Content { // TODO: respect z-levels
				u := getOrMakeUnit(x, y, i)
				unitsByLayers[u.Layer] = append(unitsByLayers[u.Layer], u)
			}
		}
	}

	c.UnitsByLayers = unitsByLayers
	log.Println("[bucket] chunk updated:", c.MapBounds)
}
