package bucket

import (
	"strongdmm/dm"
	"strongdmm/dm/dmicon"
	"strongdmm/dm/dmmap/dmminstance"
)

// unit stores render information about specific object instance on the map.
type unit struct {
	Sp *dmicon.Sprite

	Layer      float32
	ViewBounds Bounds

	R, G, B, A float32
}

type unitHash struct {
	x, y int
	id   uint64
}

type unitsCache struct {
	units map[unitHash]unit
}

func (u *unitsCache) Free() {
	u.units = make(map[unitHash]unit)
}

var UnitsCache = &unitsCache{make(map[unitHash]unit)}

func getOrMakeUnit(x, y int, i *dmminstance.Instance) unit {
	hash := unitHash{x: x, y: y, id: i.Id}
	if cachedUnit, ok := UnitsCache.units[hash]; ok {
		return cachedUnit
	}
	u := makeUnit(x, y, i)
	UnitsCache.units[hash] = u
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
		Bounds{x1, y1, x2, y2},
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
