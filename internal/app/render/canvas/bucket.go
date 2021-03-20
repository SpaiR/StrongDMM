package canvas

import (
	"sort"

	"github.com/SpaiR/strongdmm/pkg/dm"
	"github.com/SpaiR/strongdmm/pkg/dm/dmicon"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
)

type bucket struct {
	Units []unit
	Data  []float32
}

type unit struct {
	idx int

	sp    *dmicon.Sprite
	depth float32

	x1, y1 float32
	x2, y2 float32
}

func (u unit) pushIndices(out *[]uint32) {
	idx := uint32(u.idx * 4)
	*out = append(*out, idx+0, idx+1, idx+2, idx+1, idx+3, idx+2)
}

func createBucket(dmm *dmmap.Dmm) *bucket {
	var units []unit
	var data []float32

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
				x1 := float32((x-1)*32 + pixelX + stepX)
				y1 := float32((y-1)*32 + pixelY + stepY)
				x2 := x1 + float32(sp.IconWidth())
				y2 := y1 + float32(sp.IconHeight())
				var r, g, b, a float32 = 1, 1, 1, 1 // TODO: color extraction
				depth := countDepth(i)

				units = append(units, unit{
					idx, sp, depth,
					x1, y1, x2, y2,
				})

				data = append(data,
					x1, y1, r, g, b, a, sp.U1, sp.V2,
					x2, y1, r, g, b, a, sp.U2, sp.V2,
					x1, y2, r, g, b, a, sp.U1, sp.V1,
					x2, y2, r, g, b, a, sp.U2, sp.V1,
				)

				idx++
			}
		}
	}

	sort.SliceStable(units, func(i, j int) bool {
		return units[i].depth < units[j].depth
	})

	return &bucket{
		Units: units,
		Data:  data,
	}
}

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
