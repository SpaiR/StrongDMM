package pmap

import (
	"sdmm/app/command"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

type Editor struct {
	pMap *PaneMap

	editedAreas []util.Bounds
}

// Dmm returns currently edited map.
func (e *Editor) Dmm() *dmmap.Dmm {
	return e.pMap.dmm
}

// SelectInstance selects the provided instance to edit.
func (e *Editor) SelectInstance(i *dmminstance.Instance) {
	e.pMap.app.DoSelectPrefab(i.Prefab())
	e.pMap.app.DoEditInstance(i)
}

// MoveInstanceToTop swaps the provided instance with the one which is upper.
func (e *Editor) MoveInstanceToTop(i *dmminstance.Instance) {
	e.moveInstance(e.pMap.dmm.GetTile(i.Coord()), i, true)
}

// MoveInstanceToBottom swaps the provided instance with the one which is under.
func (e *Editor) MoveInstanceToBottom(i *dmminstance.Instance) {
	e.moveInstance(e.pMap.dmm.GetTile(i.Coord()), i, false)
}

func (e *Editor) moveInstance(tile *dmmap.Tile, i *dmminstance.Instance, top bool) {
	sortedInstances := tile.Instances().Sorted()

	for idx, instance := range sortedInstances {
		if instance.Id() == i.Id() {
			var nextIdx int
			if top {
				nextIdx = idx + 1
			} else {
				nextIdx = idx - 1
			}

			if nextIdx < 0 || nextIdx >= len(sortedInstances) {
				break
			}

			nextInstance := sortedInstances[nextIdx]
			nextInstancePath := nextInstance.Prefab().Path()

			// Move the instance only if the next instance is /obj or /mob type.
			if dm.IsPath(nextInstancePath, "/obj") || dm.IsPath(nextInstancePath, "/mob") {
				sortedInstances[idx] = sortedInstances[nextIdx]
				sortedInstances[nextIdx] = instance

				tile.Set(sortedInstances)

				var commitMsg string
				if top {
					commitMsg = "Move to Top"
				} else {
					commitMsg = "Move to Bottom"
				}

				go e.CommitChanges(commitMsg)
			}

			break
		}
	}
}

// DeleteInstance deletes the provided instance from the map.
func (e *Editor) DeleteInstance(i *dmminstance.Instance) {
	tile := e.pMap.dmm.GetTile(i.Coord())
	tile.InstancesRemoveByInstance(i)
	tile.InstancesRegenerate()
	go e.CommitChanges("Delete Instance")
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
func (e *Editor) CommitChanges(commitMsg string) {
	stateId, tilesToUpdate := e.pMap.snapshot.Commit()

	// Do not push command if there is no tiles to update.
	if len(tilesToUpdate) == 0 {
		return
	}

	// Copy the value to pass it to the lambda.
	activeLevel := e.pMap.activeLevel

	// Ensure that the user has updated visuals.
	e.pMap.canvas.Render().UpdateBucketV(e.pMap.dmm, activeLevel, tilesToUpdate)

	e.pMap.app.CommandStorage().Push(command.Make(commitMsg, func() {
		e.pMap.snapshot.GoTo(stateId - 1)
		e.pMap.canvas.Render().UpdateBucketV(e.pMap.dmm, activeLevel, tilesToUpdate)
	}, func() {
		e.pMap.snapshot.GoTo(stateId)
		e.pMap.canvas.Render().UpdateBucketV(e.pMap.dmm, activeLevel, tilesToUpdate)
	}))
}
