package editor

import (
	"log"

	"sdmm/app/ui/cpwsarea/wsmap/tools"

	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

// TileCopySelected copies currently selected tiles.
// Respects a dm.PathsFilter state.
func (e *Editor) TileCopySelected() {
	e.app.Clipboard().Copy(e.app.PathsFilter(), e.dmm, tools.SelectedTiles())
}

// TilePasteSelected does a paste to the currently hovered tile.
// Pasted tiles will be automatically selected by the tools.ToolGrab.
// Respects a dm.PathsFilter state.
func (e *Editor) TilePasteSelected() {
	pasteCoord := e.pMap.CanvasState().LastHoveredTile()
	pastedData := e.app.Clipboard().Buffer()

	if len(pastedData.Buffer) == 0 {
		return
	}

	log.Printf("[pmap] paste tiles from the clipboard buffer on the map: %v", pasteCoord)

	// Fill copied tiles from the bottom-left tile.
	anchor := pastedData.Buffer[0].Coord

	// Select a "select" tool and reset its selection.
	toolSelect := tools.SetSelected(tools.TNGrab).(*tools.ToolGrab)
	toolSelect.Reset()

	// Var to store tiles with their new position.
	tilesToPaste := make(map[util.Point]dmmap.Tile)

	// Calculate tiles positions.
	var tilesToSelect []util.Point
	for _, tileCopy := range pastedData.Buffer {
		pos := util.Point{
			X: pasteCoord.X + tileCopy.Coord.X - anchor.X,
			Y: pasteCoord.Y + tileCopy.Coord.Y - anchor.Y,
			Z: pasteCoord.Z,
		}

		if !e.Dmm().HasTile(pos) {
			continue
		}

		tilesToSelect = append(tilesToSelect, pos)
		tilesToPaste[pos] = tileCopy
	}

	// Pre-select tiles we will paste onto.
	toolSelect.PreSelectArea(tilesToSelect)

	for pos, tileCopy := range tilesToPaste {
		tile := e.Dmm().GetTile(pos)

		currTilePrefabs := tile.Instances().Prefabs()
		newTilePrefabs := make(dmmdata.Prefabs, 0, len(currTilePrefabs))

		// Keep instances which are not filtered out.
		for _, prefab := range currTilePrefabs {
			if !pastedData.Filter.IsVisiblePath(prefab.Path()) {
				newTilePrefabs = append(newTilePrefabs, prefab)
			}
		}

		// And append copied instances.
		newTilePrefabs = append(newTilePrefabs, tileCopy.Instances().Prefabs()...)

		tile.InstancesSet(newTilePrefabs.Sorted())
		tile.InstancesRegenerate()
	}

	// Select tiles we've pasted.
	toolSelect.SelectArea(tilesToSelect)
}

// TileCutSelected does a cut (copy+delete) of the currently hovered tile.
// Respects a dm.PathsFilter state.
func (e *Editor) TileCutSelected() {
	e.TileCopySelected()
	e.TileDeleteSelected()
}

// TileDeleteSelected deletes the last hovered by the mouse tile.
// Respects a dm.PathsFilter state.
func (e *Editor) TileDeleteSelected() {
	for _, tile := range tools.SelectedTiles() {
		e.TileDelete(tile)
	}
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
