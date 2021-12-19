package tilemenu

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/shortcut"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmmclip"
	"sdmm/util"
)

type App interface {
	DoUndo()
	DoRedo()

	DoCopy()
	DoPaste()
	DoCut()
	DoDelete()

	PointSize() float32

	CommandStorage() *command.Storage
	Clipboard() *dmmclip.Clipboard

	HasSelectedPrefab() bool
	SelectedPrefab() (*dmmprefab.Prefab, bool)
}

type editor interface {
	Dmm() *dmmap.Dmm

	CommitChanges(string)

	InstanceSelect(i *dmminstance.Instance)
	InstanceMoveToTop(i *dmminstance.Instance)
	InstanceMoveToBottom(i *dmminstance.Instance)
	InstanceDelete(i *dmminstance.Instance)
	InstanceReplace(i *dmminstance.Instance, prefab *dmmprefab.Prefab)
	InstanceReset(i *dmminstance.Instance)
}

type TileMenu struct {
	shortcuts shortcut.Shortcuts

	app    App
	editor editor

	opened bool

	tile *dmmap.Tile
}

func New(app App, editor editor) *TileMenu {
	t := &TileMenu{app: app, editor: editor}
	t.addShortcuts()
	return t
}

func (t *TileMenu) Dispose() {
	t.close()
	t.shortcuts.Dispose()
}

func (t *TileMenu) Open(coord util.Point) {
	if t.editor.Dmm().HasTile(coord) {
		t.tile = t.editor.Dmm().GetTile(coord)
		t.opened = true
		imgui.OpenPopup("tileMenu")
	}
}

func (t *TileMenu) close() {
	t.opened = false
	t.tile = nil
}
