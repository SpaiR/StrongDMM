package render

import (
	"sort"

	"github.com/SpaiR/strongdmm/pkg/dm"
	"github.com/SpaiR/strongdmm/pkg/dm/dmicon"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
)

// bucket contains data needed to render the map.
type bucket struct {
	// When true, means that the bucket updates its data on the background at the moment.
	updating bool

	// Cached units to avoid unneeded objects creation.
	unitsCache map[unitHash]unit

	layers        []float32
	unitsByLayers map[float32][]unit
}

type unitHash struct {
	x, y int
	id   uint64
}

func newBucket() *bucket {
	return &bucket{
		unitsCache: make(map[unitHash]unit),
	}
}

// unit stores all needed information to render one specific map instance.
type unit struct {
	sp *dmicon.Sprite

	layer float32

	x1, y1 float32
	x2, y2 float32

	r, g, b, a float32
}

func (u unit) isInBounds(x1, y1, x2, y2 float32) bool {
	return u.x2 >= x1 && u.y2 >= y1 && u.x1 <= x2 && u.y1 <= y2
}

// A very first update cycle will go in the main thread, others will go on the background.
// This will help application to run smoothly.
func (bu *bucket) update(dmm *dmmap.Dmm) {
	if bu.unitsByLayers != nil {
		go bu._update(dmm)
	} else {
		bu._update(dmm)
	}
}

func (bu *bucket) _update(dmm *dmmap.Dmm) {
	bu.updating = true

	// Create a storage for our units by layers with initial capacity.
	// Inner slices are created with initial capacity as well.
	unitsByLayers := make(map[float32][]unit, len(bu.unitsByLayers))
	for layer := range bu.unitsByLayers {
		unitsByLayers[layer] = make([]unit, 0, len(bu.unitsByLayers[layer]))
	}

	for x := 1; x <= dmm.MaxX; x++ {
		for y := 1; y <= dmm.MaxY; y++ {
			for _, i := range dmm.GetTile(x, y, 1).Content { // TODO: respect z-levels
				u := bu.getOrMakeUnit(x, y, i)
				unitsByLayers[u.layer] = append(unitsByLayers[u.layer], u)
			}
		}
	}

	// Sort units by layers to represent instances layering properly.
	layers := make([]float32, 0, len(unitsByLayers))
	for layer := range unitsByLayers {
		if len(unitsByLayers[layer]) > 0 {
			layers = append(layers, layer)
		}
	}
	sort.Slice(layers, func(i, j int) bool { return layers[i] < layers[j] })

	bu.layers = layers
	bu.unitsByLayers = unitsByLayers
	bu.updating = false
}

func (bu *bucket) getOrMakeUnit(x, y int, i *dmminstance.Instance) unit {
	hash := unitHash{x: x, y: y, id: i.Id}
	if cachedUnit, ok := bu.unitsCache[hash]; ok {
		return cachedUnit
	}
	u := makeUnit(x, y, i)
	bu.unitsCache[hash] = u
	return u
}

func makeUnit(x, y int, i *dmminstance.Instance) unit {
	icon, _ := i.Vars.Text("icon")
	iconState, _ := i.Vars.Text("icon_state")
	dir, _ := i.Vars.Int("dir")
	pixelX, _ := i.Vars.Int("pixel_x")
	pixelY, _ := i.Vars.Int("pixel_y")
	stepX, _ := i.Vars.Int("step_x")
	stepY, _ := i.Vars.Int("step_y")

	sp := dmicon.Cache.GetSpriteOrPlaceholderV(icon, iconState, dir)
	x1 := float32((x-1)*32 + pixelX + stepX) // TODO: world icon_size
	y1 := float32((y-1)*32 + pixelY + stepY) // TODO: world icon_size
	x2 := x1 + float32(sp.IconWidth())
	y2 := y1 + float32(sp.IconHeight())
	var r, g, b, a float32 = 1, 1, 1, 1 // TODO: color extraction

	return unit{
		sp, countLayer(i),
		x1, y1, x2, y2,
		r, g, b, a,
	}
}

// countLayer returns the value of combined instance vars: plane + layer.
func countLayer(i *dmminstance.Instance) float32 {
	plane, _ := i.Vars.Float("plane")
	layer, _ := i.Vars.Float("layer")

	layer = plane*10_000 + layer*1000

	// When mobs are on the same layer with object they are always rendered above them (BYOND specific stuff).
	if dm.IsPath(i.Path, "/obj") {
		layer += 100
	} else if dm.IsPath(i.Path, "/mob") {
		layer += 10
	}

	return layer
}
