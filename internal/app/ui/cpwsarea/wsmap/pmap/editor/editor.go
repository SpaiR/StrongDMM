package editor

import (
	"sdmm/internal/app/command"
	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap/canvas"
	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap/overlay"
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/dmapi/dmmap/dmminstance"
	"sdmm/internal/dmapi/dmmclip"
	"sdmm/internal/dmapi/dmmsnap"
	"sdmm/internal/util"

	"github.com/SpaiR/imgui-go"
)

type Editor struct {
	app  app
	pMap attachedMap

	dmm *dmmap.Dmm

	flickAreas    []overlay.FlickArea
	flickInstance []overlay.FlickInstance

	areasZones []AreaZone
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

func (e *Editor) AreasZones() []AreaZone {
	return e.areasZones
}

func (e *Editor) ActiveLevel() int {
	return e.pMap.ActiveLevel()
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
	SetActiveLevel(int)

	Snapshot() *dmmsnap.DmmSnap

	Size() imgui.Vec2

	Canvas() *canvas.Canvas
	CanvasState() *canvas.State
	CanvasControl() *canvas.Control
	CanvasOverlay() *canvas.Overlay

	PushAreaHover(bounds util.Bounds, fillColor, borderColor util.Color)

	OnMapSizeChange()
}

func New(app app, attachedMap attachedMap, dmm *dmmap.Dmm) *Editor {
	e := &Editor{
		app:  app,
		pMap: attachedMap,
		dmm:  dmm,
	}
	e.updateAreasZones()
	return e
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

	camera := e.pMap.Canvas().Render().Camera
	camera.ShiftX = e.pMap.Size().X/2/camera.Scale + float32(absPos.X)
	camera.ShiftY = e.pMap.Size().Y/2/camera.Scale + float32(absPos.Y)

	e.pMap.SetActiveLevel(relPos.Z)
}

// FocusCameraOnPosition centers the camera on given coordinates.
func (e *Editor) FocusCameraOnPosition(coord util.Point) {
	absPos := util.Point{X: (coord.X - 1) * -dmmap.WorldIconSize, Y: (coord.Y - 1) * -dmmap.WorldIconSize, Z: coord.Z}

	camera := e.pMap.Canvas().Render().Camera
	camera.ShiftX = e.pMap.Size().X/2/camera.Scale + float32(absPos.X)
	camera.ShiftY = e.pMap.Size().Y/2/camera.Scale + float32(absPos.Y)

	e.pMap.SetActiveLevel(coord.Z)
	e.OverlaySetTileFlick(coord)
}
