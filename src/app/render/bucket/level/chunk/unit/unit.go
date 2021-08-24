package unit

import (
	"sdmm/dm"
	"sdmm/dm/dmicon"
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

var Cache = &unitsCache{make(map[unitHash]Unit)}

// Unit stores render information about specific object instance on the map.
type Unit struct {
	Sp *dmicon.Sprite

	Layer      float32
	ViewBounds util.Bounds

	R, G, B, A float32
}

type unitHash struct {
	x, y int
	id   uint64
}

type unitsCache struct {
	units map[unitHash]Unit
}

func (u *unitsCache) Free() {
	u.units = make(map[unitHash]Unit)
}

func (u *unitsCache) Get(x, y int, i *dmminstance.Instance, iconSize int) Unit {
	hash := unitHash{x: x, y: y, id: i.Id}
	if cachedUnit, ok := u.units[hash]; ok {
		return cachedUnit
	}
	unit := makeUnit(x, y, i, iconSize)
	u.units[hash] = unit
	return unit
}

func makeUnit(x, y int, i *dmminstance.Instance, iconSize int) Unit {
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
	var r, g, b, a float32 = 1, 1, 1, 1 // FIXME: color extraction

	return Unit{
		sp, countLayer(i),
		util.Bounds{X1: x1, Y1: y1, X2: x2, Y2: y2},
		r, g, b, a,
	}
}

// countLayer returns the value of combined instance vars: plane + Layer.
func countLayer(i *dmminstance.Instance) float32 {
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
