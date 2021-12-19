package pmap

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/layout/lnode"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

type Editor struct {
	pMap *PaneMap

	flickAreas    []flickArea
	flickInstance []flickInstance
}

// Dmm returns currently edited map.
func (e *Editor) Dmm() *dmmap.Dmm {
	return e.pMap.dmm
}

// SelectInstance selects the provided instance to edit.
func (e *Editor) SelectInstance(i *dmminstance.Instance) {
	e.pMap.app.DoSelectPrefab(i.Prefab())
	e.pMap.app.ShowLayout(lnode.NamePrefabs, false)
	e.pMap.app.DoEditInstance(i)
	e.pMap.app.ShowLayout(lnode.NameVariables, false)
}

func (e *Editor) HoveredInstance() *dmminstance.Instance {
	return e.pMap.canvasState.HoveredInstance()
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
			}

			return
		}
	}
}

// DeleteInstance deletes the provided instance from the map.
func (e *Editor) DeleteInstance(i *dmminstance.Instance) {
	tile := e.pMap.dmm.GetTile(i.Coord())
	tile.InstancesRemoveByInstance(i)
	tile.InstancesRegenerate()
}

// DeleteInstancesByPrefab deletes from the map all instances from the provided prefab.
func (e *Editor) DeleteInstancesByPrefab(prefab *dmmprefab.Prefab) {
	instances := e.FindInstancesByPrefabId(prefab.Id())
	for _, instance := range instances {
		tile := e.pMap.dmm.GetTile(instance.Coord())
		tile.InstancesRemoveByInstance(instance)
		tile.InstancesRegenerate()
	}
}

// ReplaceInstance replaces the provided instance with the provided prefab.
func (e *Editor) ReplaceInstance(i *dmminstance.Instance, prefab *dmmprefab.Prefab) {
	tile := e.pMap.dmm.GetTile(i.Coord())

	instances := tile.Instances()
	for _, instance := range instances {
		if instance.Id() == i.Id() {
			if dm.IsPathBaseSame(instance.Prefab().Path(), prefab.Path()) {
				instance.SetPrefab(prefab)
			}
			return
		}
	}
}

// ResetInstance resets the provided instance to the initial state (no custom variables).
func (e *Editor) ResetInstance(i *dmminstance.Instance) {
	i.SetPrefab(dmmap.PrefabStorage.Initial(i.Prefab().Path()))
}

// UpdateCanvasByCoords updates the canvas for the provided coords.
func (e *Editor) UpdateCanvasByCoords(coords []util.Point) {
	e.pMap.canvas.Render().UpdateBucketV(e.pMap.dmm, e.pMap.activeLevel, coords)
}

// UpdateCanvasByTiles updates the canvas for the provided tiles.
func (e *Editor) UpdateCanvasByTiles(tiles []dmmap.Tile) {
	coords := make([]util.Point, 0, len(tiles))
	for _, tile := range tiles {
		coords = append(coords, tile.Coord)
	}
	e.UpdateCanvasByCoords(coords)
}

// SelectedPrefab returns a currently selected prefab.
func (e *Editor) SelectedPrefab() (*dmmprefab.Prefab, bool) {
	return e.pMap.app.SelectedPrefab()
}

// CopyHoveredTile copies currently hovered tiles.
// Respects a dm.PathsFilter state.
func (e *Editor) CopyHoveredTile() {
	tile := []util.Point{e.pMap.canvasState.LastHoveredTile()}
	e.pMap.app.Clipboard().Copy(e.pMap.app.PathsFilter(), e.pMap.dmm, tile)
}

// PasteHoveredTile does a paste to the currently hovered tile.
// Respects a dm.PathsFilter state.
func (e *Editor) PasteHoveredTile() {
	e.pMap.app.Clipboard().Paste(e.pMap.dmm, e.pMap.canvasState.LastHoveredTile())
}

// CutHoveredTile does a cut (copy+delete) of the currently hovered tile.
// Respects a dm.PathsFilter state.
func (e *Editor) CutHoveredTile() {
	e.CopyHoveredTile()
	e.DeleteHoveredTile()
}

// DeleteHoveredTile deletes the last hovered by the mouse tile.
// Respects a dm.PathsFilter state.
func (e *Editor) DeleteHoveredTile() {
	e.DeleteTile(e.pMap.canvasState.LastHoveredTile())
}

// DeleteTile deletes content of the tile with the provided coord.
// Respects a dm.PathsFilter state.
func (e *Editor) DeleteTile(coord util.Point) {
	tile := e.pMap.dmm.GetTile(coord)
	e.deleteTileContent(tile)
	tile.InstancesRegenerate()
}

func (e *Editor) deleteTileContent(tile *dmmap.Tile) {
	for _, instance := range tile.Instances() {
		if e.pMap.app.PathsFilter().IsVisiblePath(instance.Prefab().Path()) {
			tile.InstancesRemoveByPath(instance.Prefab().Path())
		}
	}
}

// ReplaceTile replaces content of the tile with the provided coord with provided prefabs.
// Respects a dm.PathsFilter state.
func (e *Editor) ReplaceTile(coord util.Point, prefabs dmmdata.Prefabs) {
	tile := e.pMap.dmm.GetTile(coord)

	e.deleteTileContent(tile)

	for _, prefab := range prefabs {
		if e.pMap.app.PathsFilter().IsVisiblePath(prefab.Path()) {
			tile.InstancesAdd(prefab)
		}
	}

	tile.InstancesRegenerate()
}

// ReplacePrefab replaces all old prefabs on the map with the new one. Commits map changes.
func (e *Editor) ReplacePrefab(oldPrefab, newPrefab *dmmprefab.Prefab) {
	for _, tile := range e.pMap.dmm.Tiles {
		for _, instance := range tile.Instances() {
			if instance.Prefab().Id() == oldPrefab.Id() {
				instance.SetPrefab(newPrefab)
			}
		}
	}
}

// PushOverlayTile pushes tile overlay for the next frame.
func (e *Editor) PushOverlayTile(coord util.Point, colFill, colBorder util.Color) {
	e.PushOverlayArea(util.Bounds{
		X1: float32(coord.X),
		Y1: float32(coord.Y),
		X2: float32(coord.X),
		Y2: float32(coord.Y),
	}, colFill, colBorder)
}

// PushOverlayArea pushes area overlay for the next frame.
func (e *Editor) PushOverlayArea(area util.Bounds, colFill, colBorder util.Color) {
	e.pMap.pushAreaHover(util.Bounds{
		X1: (area.X1 - 1) * float32(dmmap.WorldIconSize),
		Y1: (area.Y1 - 1) * float32(dmmap.WorldIconSize),
		X2: (area.X2-1)*float32(dmmap.WorldIconSize) + float32(dmmap.WorldIconSize),
		Y2: (area.Y2-1)*float32(dmmap.WorldIconSize) + float32(dmmap.WorldIconSize),
	}, colFill, colBorder)
}

// SetOverlayTileFlick sets for the provided tile a flick overlay.
// Unlike the PushOverlayTile or PushOverlayArea methods, flick overlay is set only once.
// It will exist until it disappears.
func (e *Editor) SetOverlayTileFlick(coord util.Point) {
	e.flickAreas = append(e.flickAreas, flickArea{
		time: imgui.Time(),
		area: util.Bounds{
			X1: float32((coord.X - 1) * dmmap.WorldIconSize),
			Y1: float32((coord.Y - 1) * dmmap.WorldIconSize),
			X2: float32((coord.X-1)*dmmap.WorldIconSize + dmmap.WorldIconSize),
			Y2: float32((coord.Y-1)*dmmap.WorldIconSize + dmmap.WorldIconSize),
		},
	})
}

// SetOverlayInstanceFlick sets for the provided instance a flick overlay.
// Unlike the PushOverlayTile or PushOverlayArea methods, flick overlay is set only once.
// It will exist until it disappears.
func (e *Editor) SetOverlayInstanceFlick(i *dmminstance.Instance) {
	e.flickInstance = append(e.flickInstance, flickInstance{
		time:     imgui.Time(),
		instance: i,
	})
}

// CommitChanges triggers a snapshot to commit changes and create a patch between two map states.
func (e *Editor) CommitChanges(commitMsg string) {
	go e.commitChanges(commitMsg)
}

// Used as a wrapper to do a stuff inside the goroutine.
func (e *Editor) commitChanges(commitMsg string) {
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
		e.pMap.dmm.PersistPrefabs()
		e.pMap.app.SyncPrefabs()
		e.pMap.app.SyncVarEditor()
	}, func() {
		e.pMap.snapshot.GoTo(stateId)
		e.pMap.canvas.Render().UpdateBucketV(e.pMap.dmm, activeLevel, tilesToUpdate)
		e.pMap.dmm.PersistPrefabs()
		e.pMap.app.SyncPrefabs()
		e.pMap.app.SyncVarEditor()
	}))
}

// FindInstancesByPrefabId returns all instances from the current map with a corresponding prefab ID.
func (e *Editor) FindInstancesByPrefabId(prefabId uint64) (result []*dmminstance.Instance) {
	for _, tile := range e.pMap.dmm.Tiles {
		for _, instance := range tile.Instances() {
			if instance.Prefab().Id() == prefabId {
				result = append(result, instance)
			}
		}
	}
	return result
}

// FocusCamera moves the camera in a way, so it will be centered on the instance.
func (e *Editor) FocusCamera(i *dmminstance.Instance) {
	relPos := i.Coord()
	absPos := util.Point{X: (relPos.X - 1) * -dmmap.WorldIconSize, Y: (relPos.Y - 1) * -dmmap.WorldIconSize, Z: relPos.Z}

	camera := e.pMap.canvas.Render().Camera()
	camera.ShiftX = e.pMap.size.X/2/camera.Scale + float32(absPos.X)
	camera.ShiftY = e.pMap.size.Y/2/camera.Scale + float32(absPos.Y)
}
