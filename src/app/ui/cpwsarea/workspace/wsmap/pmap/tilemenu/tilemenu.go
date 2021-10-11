package tilemenu

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/shortcut"
	"sdmm/dm/dmmap"
	"sdmm/util"
)

type Action interface {
	AppDoUndo()
	AppDoRedo()

	AppHasUndo() bool
	AppHasRedo() bool
}

type mapState interface {
	Dmm() *dmmap.Dmm
}

type TileMenu struct {
	shortcuts shortcut.Shortcuts

	action   Action
	mapState mapState

	opened bool

	tile *dmmap.Tile
}

func New(action Action, state mapState) *TileMenu {
	t := &TileMenu{action: action, mapState: state}
	t.addShortcuts()
	return t
}

func (t *TileMenu) Dispose() {
	t.close()
	t.shortcuts.Dispose()
}

func (t *TileMenu) Open(coord util.Point) {
	if t.mapState.Dmm().HasTile(coord) {
		t.tile = t.mapState.Dmm().GetTile(coord)
		t.opened = true
		imgui.OpenPopup("tileMenu")
	}
}

func (t *TileMenu) Process() {
	if !t.opened {
		return
	}

	if imgui.BeginPopup("tileMenu") {
		t.showControls()
		imgui.EndPopup()
	} else {
		t.close()
	}
}

func (t *TileMenu) close() {
	t.opened = false
	t.tile = nil
}
