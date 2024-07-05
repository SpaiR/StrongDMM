package unit

import (
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmicon"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/dmapi/dmmap/dmminstance"
	"sdmm/internal/util"
)

// Unit stores render information about specific object prefab on the map.
type Unit struct {
	sprite   *dmicon.Sprite
	instance *dmminstance.Instance

	layer      float32
	viewBounds util.Bounds

	r, g, b, a float32
}

func (u Unit) Sprite() *dmicon.Sprite {
	return u.sprite
}

func (u Unit) Instance() *dmminstance.Instance {
	return u.instance
}

func (u Unit) Layer() float32 {
	return u.layer
}

func (u Unit) ViewBounds() util.Bounds {
	return u.viewBounds
}

func (u Unit) R() float32 {
	return u.r
}

func (u Unit) G() float32 {
	return u.g
}

func (u Unit) B() float32 {
	return u.b
}

func (u Unit) A() float32 {
	return u.a
}

func Make(x, y int, i *dmminstance.Instance, iconSize int) Unit {
	// All vars below are built-in and expected to exist.
	icon, _ := i.Prefab().Vars().Text("icon")
	iconState, _ := i.Prefab().Vars().Text("icon_state")
	dir, _ := i.Prefab().Vars().Int("dir")
	pixelX, _ := i.Prefab().Vars().Int("pixel_x")
	pixelY, _ := i.Prefab().Vars().Int("pixel_y")
	stepX, _ := i.Prefab().Vars().Int("step_x")
	stepY, _ := i.Prefab().Vars().Int("step_y")

	sp := dmicon.Cache.GetSpriteOrPlaceholderV(icon, iconState, dir)
	x1 := float32((x-1)*iconSize + pixelX + stepX)
	y1 := float32((y-1)*iconSize + pixelY + stepY)
	x2 := x1 + float32(sp.IconWidth())
	y2 := y1 + float32(sp.IconHeight())
	r, g, b, a := parseColor(i.Prefab())

	return Unit{
		sp, i, countLayer(i.Prefab()),
		util.Bounds{X1: x1, Y1: y1, X2: x2, Y2: y2},
		r, g, b, a,
	}
}

func parseColor(p *dmmprefab.Prefab) (r, g, b, a float32) {
	// Default rgba is white.
	r, g, b, a = 1, 1, 1, 1
	if color, _ := p.Vars().Text("color"); color != "" {
		r, g, b, _ = util.ParseColor(color).RGBA()
		alpha := p.Vars().FloatV("alpha", 255)
		a = alpha / 255 // Color = RGB from color variable + alpha variable.
	}
	return r, g, b, a
}

// countLayer returns the value of combined prefab vars: plane + Layer.
func countLayer(p *dmmprefab.Prefab) float32 {
	plane, _ := p.Vars().Float("plane")
	layer, _ := p.Vars().Float("layer")

	// Way larger then the "logical" max of (max normal layer value) + (max effect layer value) -> (4999) + (20000)
	const layer_max = 40000 * 1000
	
	layer = plane * (layer_max + 1) + layer*1000

	// When mobs are on the same Layer with object they are always rendered above them (BYOND specific stuff).
	if dm.IsPath(p.Path(), "/obj") {
		layer += 100
	} else if dm.IsPath(p.Path(), "/mob") {
		layer += 10
	}

	return layer
}
