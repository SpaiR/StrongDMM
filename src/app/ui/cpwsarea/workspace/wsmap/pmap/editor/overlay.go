package editor

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/overlay"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

// OverlayPushTile pushes tile overlay for the next frame.
func (e *Editor) OverlayPushTile(coord util.Point, colFill, colBorder util.Color) {
	e.OverlayPushArea(util.Bounds{
		X1: float32(coord.X),
		Y1: float32(coord.Y),
		X2: float32(coord.X),
		Y2: float32(coord.Y),
	}, colFill, colBorder)
}

// OverlayPushArea pushes area overlay for the next frame.
func (e *Editor) OverlayPushArea(area util.Bounds, colFill, colBorder util.Color) {
	e.pMap.PushAreaHover(util.Bounds{
		X1: (area.X1 - 1) * float32(dmmap.WorldIconSize),
		Y1: (area.Y1 - 1) * float32(dmmap.WorldIconSize),
		X2: (area.X2-1)*float32(dmmap.WorldIconSize) + float32(dmmap.WorldIconSize),
		Y2: (area.Y2-1)*float32(dmmap.WorldIconSize) + float32(dmmap.WorldIconSize),
	}, colFill, colBorder)
}

// OverlaySetTileFlick sets for the provided tile a flick overlay.
// Unlike the PushOverlayTile or PushOverlayArea methods, flick overlay is set only once.
// It will exist until it disappears.
func (e *Editor) OverlaySetTileFlick(coord util.Point) {
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

// OverlaySetInstanceFlick sets for the provided instance a flick overlay.
// Unlike the PushOverlayTile or PushOverlayArea methods, flick overlay is set only once.
// It will exist until it disappears.
func (e *Editor) OverlaySetInstanceFlick(i *dmminstance.Instance) {
	e.flickInstance = append(e.flickInstance, overlay.FlickInstance{
		Time:     imgui.Time(),
		Instance: i,
	})
}
