package tools

import (
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/util"
)

// Tool is a basic interface for tools in the panel.
type Tool interface {
	Name() string

	Stale() bool
	AltBehaviour() bool
	setAltBehaviour(bool)

	// OnDeselect gees when the current tool is deselected.
	OnDeselect()

	// Goes every app cycle to handle stuff like pushing overlays etc.
	process()
	// Goes when user clicks on the map.
	onStart(coord util.Point)
	// Goes when user clicked and, while holding the mouse button, move the mouse.
	onMove(coord util.Point)
	// Goes when user releases the mouse button.
	onStop(coord util.Point)
}

// Tool is a basic interface for tools in the panel.
type tool struct {
	altBehaviour bool
}

func (t *tool) Stale() bool {
	return true
}

func (t *tool) AltBehaviour() bool {
	return t.altBehaviour
}

func (t *tool) setAltBehaviour(altBehaviour bool) {
	t.altBehaviour = altBehaviour
}

func (tool) process() {
}

func (tool) onStart(util.Point) {
}

func (tool) onMove(util.Point) {
}

func (tool) onStop(util.Point) {
}

func (tool) OnDeselect() {
}

// A basic behaviour add.
// Adds object above and tile with a replacement.
// Mirrors that behaviour in the alt mode.
func (t *tool) basicPrefabAdd(tile *dmmap.Tile, prefab *dmmprefab.Prefab) {
	if !t.altBehaviour {
		if dm.IsPath(prefab.Path(), "/area") {
			tile.InstancesRemoveByPath("/area")
		} else if dm.IsPath(prefab.Path(), "/turf") {
			tile.InstancesRemoveByPath("/turf")
		}
	} else if dm.IsPath(prefab.Path(), "/obj") {
		tile.InstancesRemoveByPath("/obj")
	}

	tile.InstancesAdd(prefab)
	tile.InstancesRegenerate()
}
