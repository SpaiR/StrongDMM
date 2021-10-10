package unit

import (
	"log"

	"github.com/mazznoer/csscolorparser"
	"sdmm/dm"
	"sdmm/dm/dmicon"
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

// Unit stores render information about specific object instance on the map.
type Unit struct {
	Sp   *dmicon.Sprite
	Inst *dmminstance.Instance

	Layer      float32
	ViewBounds util.Bounds

	R, G, B, A float32
}

func Make(x, y int, i dmminstance.Instance, iconSize int) Unit {
	// All vars below are built-in and expected to exist.
	icon, _ := i.Vars.Text("icon")
	iconState, _ := i.Vars.Text("icon_state")
	dir, _ := i.Vars.Int("dir")
	pixelX, _ := i.Vars.Int("pixel_x")
	pixelY, _ := i.Vars.Int("pixel_y")
	stepX, _ := i.Vars.Int("step_x")
	stepY, _ := i.Vars.Int("step_y")

	sp := dmicon.Cache.GetSpriteOrPlaceholderV(icon, iconState, dir)
	x1 := float32((x-1)*iconSize + pixelX + stepX)
	y1 := float32((y-1)*iconSize + pixelY + stepY)
	x2 := x1 + float32(sp.IconWidth())
	y2 := y1 + float32(sp.IconHeight())
	r, g, b, a := parseColor(i)

	return Unit{
		sp, &i, countLayer(i),
		util.Bounds{X1: x1, Y1: y1, X2: x2, Y2: y2},
		r, g, b, a,
	}
}

func parseColor(i dmminstance.Instance) (r, g, b, a float32) {
	// Default rgba is white.
	r, g, b, a = 1, 1, 1, 1
	if color, _ := i.Vars.Text("color"); color != "" {
		if c, err := csscolorparser.Parse(color); err == nil {
			// Color = RGB from color variable + alpha variable.
			alpha, _ := i.Vars.Float("alpha")
			r, g, b, a = float32(c.R), float32(c.G), float32(c.B), alpha/255
		} else {
			log.Printf("[unit] unable to parse [%s] for [%s]: [%v]", color, i.Path, err)
		}
	}
	return r, g, b, a
}

// countLayer returns the value of combined instance vars: plane + Layer.
func countLayer(i dmminstance.Instance) float32 {
	plane, _ := i.Vars.Float("plane")
	layer, _ := i.Vars.Float("layer")

	layer = plane*10_000 + layer*1000

	// When mobs are on the same Layer with object they are always rendered above them (BYOND specific stuff).
	if dm.IsPath(i.Path, "/obj") {
		layer += 100
	} else if dm.IsPath(i.Path, "/mob") {
		layer += 10
	}

	return layer
}
