package editor

import (
	"log"

	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

// TileCopyHovered copies currently hovered tiles.
// Respects a dm.PathsFilter state.
func (e *Editor) TileCopyHovered() {
	e.app.Clipboard().Copy(e.app.PathsFilter(), e.dmm, tools.SelectedTiles())
}

// TilePasteHovered does a paste to the currently hovered tile.
// Respects a dm.PathsFilter state.
func (e *Editor) TilePasteHovered() {
	tools.SetSelected(tools.TNSelect)

	pasteCoord := e.pMap.CanvasState().LastHoveredTile()
	pastedData := e.app.Clipboard().Buffer()

	if len(pastedData.Buffer) == 0 {
		return
	}

	log.Printf("[pmap] paste tiles from the clipboard buffer on the map: %v", pasteCoord)

	anchor := pastedData.Buffer[0].Coord

	for _, tileCopy := range pastedData.Buffer {
		pos := util.Point{
			X: pasteCoord.X + tileCopy.Coord.X - anchor.X,
			Y: pasteCoord.Y + tileCopy.Coord.Y - anchor.Y,
			Z: pasteCoord.Z,
		}

		if !e.Dmm().HasTile(pos) {
			continue
		}

		tile := e.Dmm().GetTile(pos)
		currTilePrefabs := tile.Instances().Prefabs()
		newTilePrefabs := make(dmmdata.Prefabs, 0, len(currTilePrefabs))

		for _, prefab := range currTilePrefabs {
			if !pastedData.Filter.IsVisiblePath(prefab.Path()) {
				newTilePrefabs = append(newTilePrefabs, prefab)
			}
		}

		newTilePrefabs = append(newTilePrefabs, tileCopy.Instances().Prefabs()...)

		tile.InstancesSet(newTilePrefabs.Sorted())
		tile.InstancesRegenerate()
	}
}

// TileCutHovered does a cut (copy+delete) of the currently hovered tile.
// Respects a dm.PathsFilter state.
func (e *Editor) TileCutHovered() {
	e.TileCopyHovered()
	e.TileDeleteHovered()
}

// TileDeleteHovered deletes the last hovered by the mouse tile.
// Respects a dm.PathsFilter state.
func (e *Editor) TileDeleteHovered() {
	e.TileDelete(e.pMap.CanvasState().LastHoveredTile())
}

// TileDelete deletes content of the tile with the provided coord.
// Respects a dm.PathsFilter state.
func (e *Editor) TileDelete(coord util.Point) {
	tile := e.dmm.GetTile(coord)
	e.tileDelete(tile)
	tile.InstancesRegenerate()
}

func (e *Editor) tileDelete(tile *dmmap.Tile) {
	for _, instance := range tile.Instances() {
		if e.app.PathsFilter().IsVisiblePath(instance.Prefab().Path()) {
			tile.InstancesRemoveByPath(instance.Prefab().Path())
		}
	}
}

// TileReplace replaces content of the tile with the provided coord with provided prefabs.
// Respects a dm.PathsFilter state.
func (e *Editor) TileReplace(coord util.Point, prefabs dmmdata.Prefabs) {
	tile := e.dmm.GetTile(coord)

	e.tileDelete(tile)

	for _, prefab := range prefabs {
		if e.app.PathsFilter().IsVisiblePath(prefab.Path()) {
			tile.InstancesAdd(prefab)
		}
	}

	tile.InstancesRegenerate()
}
