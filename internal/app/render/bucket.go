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
	units []unit
	data  []float32
}

// bucket unit to store data about separate item on the map.
type unit struct {
	idx int

	sp    *dmicon.Sprite
	depth float32

	x1, y1 float32
	x2, y2 float32
}

func (bu *bucket) update(dmm *dmmap.Dmm) {
	// Pre-allocated buffers with known capacity.
	bu.units = make([]unit, len(bu.units))
	bu.data = make([]float32, len(bu.data))

	idx := 0
	for x := 1; x <= dmm.MaxX; x++ {
		for y := 1; y <= dmm.MaxY; y++ {
			for _, i := range dmm.GetTile(x, y, 1).Content { // TODO: respect z-levels
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
				depth := countDepth(i)

				bu.units = append(bu.units, unit{
					idx, sp, depth,
					x1, y1, x2, y2,
				})

				bu.data = append(bu.data,
					x1, y1, r, g, b, a, sp.U1, sp.V2, // bottom-left
					x2, y1, r, g, b, a, sp.U2, sp.V2, // bottom-right
					x1, y2, r, g, b, a, sp.U1, sp.V1, // top-left
					x2, y2, r, g, b, a, sp.U2, sp.V1, // top-right
				)

				idx++
			}
		}
	}

	// Sort items by their depth.
	sort.SliceStable(bu.units, func(i, j int) bool {
		return bu.units[i].depth < bu.units[j].depth
	})
}

// countDepth will return instance depth, to provide a correct visual layering of the instance.
func countDepth(i *dmminstance.Instance) float32 {
	plane, _ := i.Vars.Float("plane")
	layer, _ := i.Vars.Float("layer")

	depth := plane*10_000 + layer*1000

	if dm.IsPath(i.Path, "/obj") {
		depth += 100
	} else if dm.IsPath(i.Path, "/mob") {
		depth += 10
	}

	return depth
}
