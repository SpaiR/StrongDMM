package editor

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/overlay"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
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

// HoveredInstance returns currently hovered instance.
func (e *Editor) HoveredInstance() *dmminstance.Instance {
	return e.pMap.CanvasState().HoveredInstance()
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

// FocusCamera moves the camera in a way, so it will be centered on the instance.
func (e *Editor) FocusCamera(i *dmminstance.Instance) {
	relPos := i.Coord()
	absPos := util.Point{X: (relPos.X - 1) * -dmmap.WorldIconSize, Y: (relPos.Y - 1) * -dmmap.WorldIconSize, Z: relPos.Z}

	camera := e.pMap.Canvas().Render().Camera()
	camera.ShiftX = e.pMap.Size().X/2/camera.Scale + float32(absPos.X)
	camera.ShiftY = e.pMap.Size().Y/2/camera.Scale + float32(absPos.Y)
}
