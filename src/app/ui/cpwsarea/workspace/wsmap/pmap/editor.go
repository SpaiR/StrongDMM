package pmap

import (
	"sdmm/app/command"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/util"
)

type Editor struct {
	pMap *PaneMap

	editedAreas []util.Bounds
}

func (e *Editor) Dmm() *dmmap.Dmm {
	return e.pMap.dmm
}

// UpdateCanvasByCoord updates the canvas for the provided point.
func (e *Editor) UpdateCanvasByCoord(coord util.Point) {
	e.pMap.canvas.Render().UpdateBucketV(e.pMap.dmm, e.pMap.activeLevel, []util.Point{coord})
}

// SelectedPrefab returns a currently selected prefab.
func (e *Editor) SelectedPrefab() (*dmmprefab.Prefab, bool) {
	return e.pMap.app.SelectedPrefab()
}

// CopyHoveredTile copies currently hovered tiles.
func (e *Editor) CopyHoveredTile() {
	e.pMap.app.Clipboard().Copy(e.pMap.dmm, []util.Point{e.pMap.canvasState.LastHoveredTile()})
}

// PasteHoveredTile does a paste to the currently hovered tile.
func (e *Editor) PasteHoveredTile() {
	e.pMap.app.Clipboard().Paste(e.pMap.dmm, e.pMap.canvasState.LastHoveredTile())
	go e.CommitChanges("Paste")
}

// CutHoveredTile does a cut (copy+delete) of the currently hovered tile.
func (e *Editor) CutHoveredTile() {
	e.CopyHoveredTile()
	e.DeleteHoveredTile()
}

// DeleteHoveredTile deletes the last hovered by the mouse tile.
func (e *Editor) DeleteHoveredTile() {
	tile := e.pMap.dmm.GetTile(e.pMap.canvasState.LastHoveredTile())

	for _, instance := range tile.Instances() {
		if e.pMap.app.PathsFilter().IsVisiblePath(instance.Prefab().Path()) {
			tile.InstancesRemoveByPath(instance.Prefab().Path())
		}
	}

	tile.InstancesRegenerate()
	go e.CommitChanges("Delete Tile")
}

// ReplacePrefab replaces all old prefabs on the map with the new one.
// Commits map changes.
func (e *Editor) ReplacePrefab(oldPrefab, newPrefab *dmmprefab.Prefab) {
	for _, tile := range e.pMap.dmm.Tiles {
		for _, instance := range tile.Instances() {
			if instance.Prefab().Id() == oldPrefab.Id() {
				instance.SetPrefab(newPrefab)
			}
		}
	}
	go e.CommitChanges("Replace Prefab")
}

func (e *Editor) MarkEditedTile(coord util.Point) {
	e.editedAreas = append(e.editedAreas, util.Bounds{
		X1: float32((coord.X - 1) * dmmap.WorldIconSize),
		Y1: float32((coord.Y - 1) * dmmap.WorldIconSize),
		X2: float32((coord.X-1)*dmmap.WorldIconSize + dmmap.WorldIconSize),
		Y2: float32((coord.Y-1)*dmmap.WorldIconSize + dmmap.WorldIconSize),
	})
}

func (e *Editor) ClearEditedTiles() {
	e.editedAreas = nil
}

// CommitChanges triggers a snapshot to commit changes and create a patch between two map states.
func (e *Editor) CommitChanges(changesType string) {
	stateId, tilesToUpdate := e.pMap.snapshot.Commit()

	// Do not push command if there is no tiles to update.
	if len(tilesToUpdate) == 0 {
		return
	}

	// Copy the value to pass it to the lambda.
	activeLevel := e.pMap.activeLevel

	// Ensure that the user has updated visuals.
	e.pMap.canvas.Render().UpdateBucketV(e.pMap.dmm, activeLevel, tilesToUpdate)

	e.pMap.app.CommandStorage().Push(command.Make(changesType, func() {
		e.pMap.snapshot.GoTo(stateId - 1)
		e.pMap.canvas.Render().UpdateBucketV(e.pMap.dmm, activeLevel, tilesToUpdate)
	}, func() {
		e.pMap.snapshot.GoTo(stateId)
		e.pMap.canvas.Render().UpdateBucketV(e.pMap.dmm, activeLevel, tilesToUpdate)
	}))
}
