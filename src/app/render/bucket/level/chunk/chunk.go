package chunk

import (
	"log"

	"sdmm/app/render/bucket/level/chunk/unit"
	"sdmm/dm/dmmap"
	"sdmm/util"
)

// Size is a maximum number of tiles per axis for a single Chunk.
const Size = 24

// Chunk stores the actual data to render.
// It stores two types of bounds: view and map.
// View bounds are a visual bounds which are used to ignore the chunk if it's out of the user viewport.
// Map bounds are coordinate points of tiles in the chunk.
type Chunk struct {
	ViewBounds, MapBounds util.Bounds

	UnitsByLayers map[float32][]unit.Unit
}

func New(x1, y1, x2, y2, iconSize float32) *Chunk {
	return &Chunk{
		ViewBounds: util.Bounds{
			X1: (x1 - 1) * iconSize,
			Y1: (y1 - 1) * iconSize,
			X2: x2 * iconSize,
			Y2: y2 * iconSize,
		},
		MapBounds: util.Bounds{
			X1: x1,
			Y1: y1,
			X2: x2,
			Y2: y2,
		},
	}
}

// Update will update internal data of the current chunk.
// Basically, we will create units for every tile in the chunk.
func (c *Chunk) Update(dmm *dmmap.Dmm, level int) {
	// Create a storage for our units by Layers with initial capacity.
	// Inner slices are created with initial capacity as well.
	unitsByLayers := make(map[float32][]unit.Unit, len(c.UnitsByLayers))
	for layer := range c.UnitsByLayers {
		unitsByLayers[layer] = make([]unit.Unit, 0, len(c.UnitsByLayers[layer]))
	}

	for x := c.MapBounds.X1; x <= c.MapBounds.X2; x++ {
		for y := c.MapBounds.Y1; y <= c.MapBounds.Y2; y++ {
			x, y := int(x), int(y)
			for _, i := range dmm.GetTile(util.Point{X: x, Y: y, Z: level}).Content {
				u := unit.Cache.Get(x, y, i, dmm.WorldIconSize)
				unitsByLayers[u.Layer] = append(unitsByLayers[u.Layer], u)
			}
		}
	}

	c.UnitsByLayers = unitsByLayers
	log.Println("[bucket] chunk updated:", c.MapBounds)
}
