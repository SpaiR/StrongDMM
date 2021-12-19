package editor

import (
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/overlay"
	"sdmm/app/ui/layout/lnode"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmmclip"
	"sdmm/dmapi/dmmsnap"
	"sdmm/util"
)

type Editor struct {
	app  app
	pMap attachedMap

	dmm *dmmap.Dmm

	flickAreas    []overlay.FlickArea
	flickInstance []overlay.FlickInstance
}

func (e *Editor) SetFlickAreas(flickAreas []overlay.FlickArea) {
	e.flickAreas = flickAreas
}

func (e *Editor) FlickAreas() []overlay.FlickArea {
	return e.flickAreas
}

func (e *Editor) SetFlickInstance(flickInstance []overlay.FlickInstance) {
	e.flickInstance = flickInstance
}

func (e *Editor) FlickInstance() []overlay.FlickInstance {
	return e.flickInstance
}

type app interface {
	DoSelectPrefab(prefab *dmmprefab.Prefab)
	DoEditInstance(*dmminstance.Instance)

	SelectedPrefab() (*dmmprefab.Prefab, bool)
	HasSelectedPrefab() bool

	AddMouseChangeCallback(cb func(uint, uint)) int
	RemoveMouseChangeCallback(id int)

	CommandStorage() *command.Storage
	Clipboard() *dmmclip.Clipboard
	PathsFilter() *dm.PathsFilter

	ShowLayout(name string, focus bool)

	SyncPrefabs()
	SyncVarEditor()
}

type attachedMap interface {
	ActiveLevel() int
	Snapshot() *dmmsnap.DmmSnap

	Size() imgui.Vec2

	Canvas() *canvas.Canvas
	CanvasState() *canvas.State
	CanvasControl() *canvas.Control
	CanvasOverlay() *canvas.Overlay

	PushAreaHover(bounds util.Bounds, fillColor, borderColor util.Color)
}

func New(app app, attachedMap attachedMap, dmm *dmmap.Dmm) *Editor {
	return &Editor{
		app:  app,
		pMap: attachedMap,
		dmm:  dmm,
	}
}

// Dmm returns currently edited map.
func (e *Editor) Dmm() *dmmap.Dmm {
	return e.dmm
}

// SelectInstance selects the provided instance to edit.
func (e *Editor) SelectInstance(i *dmminstance.Instance) {
	e.app.DoSelectPrefab(i.Prefab())
	e.app.ShowLayout(lnode.NamePrefabs, false)
	e.app.DoEditInstance(i)
	e.app.ShowLayout(lnode.NameVariables, false)
}

func (e *Editor) HoveredInstance() *dmminstance.Instance {
	return e.pMap.CanvasState().HoveredInstance()
}

// MoveInstanceToTop swaps the provided instance with the one which is upper.
func (e *Editor) MoveInstanceToTop(i *dmminstance.Instance) {
	e.moveInstance(e.dmm.GetTile(i.Coord()), i, true)
}

// MoveInstanceToBottom swaps the provided instance with the one which is under.
func (e *Editor) MoveInstanceToBottom(i *dmminstance.Instance) {
	e.moveInstance(e.dmm.GetTile(i.Coord()), i, false)
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
	tile := e.dmm.GetTile(i.Coord())
	tile.InstancesRemoveByInstance(i)
	tile.InstancesRegenerate()
}

// DeleteInstancesByPrefab deletes from the map all instances from the provided prefab.
func (e *Editor) DeleteInstancesByPrefab(prefab *dmmprefab.Prefab) {
	instances := e.FindInstancesByPrefabId(prefab.Id())
	for _, instance := range instances {
		tile := e.dmm.GetTile(instance.Coord())
		tile.InstancesRemoveByInstance(instance)
		tile.InstancesRegenerate()
	}
}

// ReplaceInstance replaces the provided instance with the provided prefab.
func (e *Editor) ReplaceInstance(i *dmminstance.Instance, prefab *dmmprefab.Prefab) {
	tile := e.dmm.GetTile(i.Coord())

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
	e.pMap.Canvas().Render().UpdateBucketV(e.dmm, e.pMap.ActiveLevel(), coords)
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
	return e.app.SelectedPrefab()
}

// CopyHoveredTile copies currently hovered tiles.
// Respects a dm.PathsFilter state.
func (e *Editor) CopyHoveredTile() {
	tile := []util.Point{e.pMap.CanvasState().LastHoveredTile()}
	e.app.Clipboard().Copy(e.app.PathsFilter(), e.dmm, tile)
}

// PasteHoveredTile does a paste to the currently hovered tile.
// Respects a dm.PathsFilter state.
func (e *Editor) PasteHoveredTile() {
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

// CutHoveredTile does a cut (copy+delete) of the currently hovered tile.
// Respects a dm.PathsFilter state.
func (e *Editor) CutHoveredTile() {
	e.CopyHoveredTile()
	e.DeleteHoveredTile()
}

// DeleteHoveredTile deletes the last hovered by the mouse tile.
// Respects a dm.PathsFilter state.
func (e *Editor) DeleteHoveredTile() {
	e.DeleteTile(e.pMap.CanvasState().LastHoveredTile())
}

// DeleteTile deletes content of the tile with the provided coord.
// Respects a dm.PathsFilter state.
func (e *Editor) DeleteTile(coord util.Point) {
	tile := e.dmm.GetTile(coord)
	e.deleteTileContent(tile)
	tile.InstancesRegenerate()
}

func (e *Editor) deleteTileContent(tile *dmmap.Tile) {
	for _, instance := range tile.Instances() {
		if e.app.PathsFilter().IsVisiblePath(instance.Prefab().Path()) {
			tile.InstancesRemoveByPath(instance.Prefab().Path())
		}
	}
}

// ReplaceTile replaces content of the tile with the provided coord with provided prefabs.
// Respects a dm.PathsFilter state.
func (e *Editor) ReplaceTile(coord util.Point, prefabs dmmdata.Prefabs) {
	tile := e.dmm.GetTile(coord)

	e.deleteTileContent(tile)

	for _, prefab := range prefabs {
		if e.app.PathsFilter().IsVisiblePath(prefab.Path()) {
			tile.InstancesAdd(prefab)
		}
	}

	tile.InstancesRegenerate()
}

// ReplacePrefab replaces all old prefabs on the map with the new one. Commits map changes.
func (e *Editor) ReplacePrefab(oldPrefab, newPrefab *dmmprefab.Prefab) {
	for _, tile := range e.dmm.Tiles {
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
	e.pMap.PushAreaHover(util.Bounds{
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
	e.flickAreas = append(e.flickAreas, overlay.FlickArea{
		Time: imgui.Time(),
		Area: util.Bounds{
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
	e.flickInstance = append(e.flickInstance, overlay.FlickInstance{
		Time:     imgui.Time(),
		Instance: i,
	})
}

// CommitChanges triggers a snapshot to commit changes and create a patch between two map states.
func (e *Editor) CommitChanges(commitMsg string) {
	go e.commitChanges(commitMsg)
}

// Used as a wrapper to do a stuff inside the goroutine.
func (e *Editor) commitChanges(commitMsg string) {
	stateId, tilesToUpdate := e.pMap.Snapshot().Commit()

	// Do not push command if there is no tiles to update.
	if len(tilesToUpdate) == 0 {
		return
	}

	// Copy the value to pass it to the lambda.
	activeLevel := e.pMap.ActiveLevel()

	// Ensure that the user has updated visuals.
	e.pMap.Canvas().Render().UpdateBucketV(e.dmm, activeLevel, tilesToUpdate)

	e.app.CommandStorage().Push(command.Make(commitMsg, func() {
		e.pMap.Snapshot().GoTo(stateId - 1)
		e.pMap.Canvas().Render().UpdateBucketV(e.dmm, activeLevel, tilesToUpdate)
		e.dmm.PersistPrefabs()
		e.app.SyncPrefabs()
		e.app.SyncVarEditor()
	}, func() {
		e.pMap.Snapshot().GoTo(stateId)
		e.pMap.Canvas().Render().UpdateBucketV(e.dmm, activeLevel, tilesToUpdate)
		e.dmm.PersistPrefabs()
		e.app.SyncPrefabs()
		e.app.SyncVarEditor()
	}))
}

// FindInstancesByPrefabId returns all instances from the current map with a corresponding prefab ID.
func (e *Editor) FindInstancesByPrefabId(prefabId uint64) (result []*dmminstance.Instance) {
	for _, tile := range e.dmm.Tiles {
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

	camera := e.pMap.Canvas().Render().Camera()
	camera.ShiftX = e.pMap.Size().X/2/camera.Scale + float32(absPos.X)
	camera.ShiftY = e.pMap.Size().Y/2/camera.Scale + float32(absPos.Y)
}
