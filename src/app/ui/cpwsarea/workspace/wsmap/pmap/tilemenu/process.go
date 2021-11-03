package tilemenu

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	w "sdmm/imguiext/widget"
	"sdmm/util"
)

func (t *TileMenu) Process() {
	t.shortcuts.SetVisible(t.opened)

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

func (t *TileMenu) showControls() {
	w.Layout{
		w.MenuItem("Undo", t.app.DoUndo).
			Enabled(t.app.CommandStorage().HasUndo()).
			Shortcut("Ctrl+Z"),
		w.MenuItem("Redo", t.app.DoRedo).
			Enabled(t.app.CommandStorage().HasRedo()).
			Shortcut("Ctrl+Shift+Z"),
		w.Separator(),
		w.MenuItem("Copy", t.app.DoCopy).
			Shortcut("Ctrl+C"),
		w.MenuItem("Paste", t.app.DoPaste).
			Enabled(t.app.Clipboard().HasData()).
			Shortcut("Ctrl+V"),
		w.MenuItem("Cut", t.app.DoCut).
			Shortcut("Ctrl+X"),
		w.MenuItem("Delete", t.app.DoDelete).
			Shortcut("Delete"),
		w.Separator(),
		w.Custom(func() {
			for idx, instance := range t.tile.Instances().Sorted() {
				t.showInstance(instance, idx)
			}
		}),
	}.Build()
}

func (t *TileMenu) showInstance(i *dmminstance.Instance, idx int) {
	p := i.Prefab()
	s := getSprite(p)
	iconSize := t.iconSize()
	r, g, b, _ := util.ParseColor(p.Vars().TextV("color", "")).RGBA()
	name := fmt.Sprintf("%s##prefab_row_%d", p.Vars().TextV("name", ""), idx)

	w.Layout{
		w.Image(imgui.TextureID(s.Texture()), iconSize, iconSize).
			Uv(imgui.Vec2{X: s.U1, Y: s.V1}, imgui.Vec2{X: s.U2, Y: s.V2}).
			TintColor(imgui.Vec4{X: r, Y: g, Z: b, W: 1}),
		w.SameLine(),
		w.Menu(name, t.showInstanceControls(i, idx)),
		w.SameLine(),
		w.Text(fmt.Sprintf("[%s]\t\t", p.Path())),
	}.Build()
}

func (t *TileMenu) showInstanceControls(i *dmminstance.Instance, idx int) w.Layout {
	p := i.Prefab()
	return w.Layout{
		w.Custom(func() {
			if dm.IsPath(p.Path(), "/obj") || dm.IsPath(p.Path(), "/mob") {
				w.Layout{
					w.MenuItem(fmt.Sprint("Move to Top##move_to_top_", idx), nil).
						Enabled(false),
					w.MenuItem(fmt.Sprint("Move to Bottom##move_to_bottom_", idx), nil).
						Enabled(false),
					w.Separator(),
				}.Build()
			}
		}),
		w.MenuItem(fmt.Sprint("Make Active Object##make_active_object_", idx), func() { t.mapState.SelectInstance(i) }).
			Enabled(false).
			Shortcut("Shift+LMB"),
		w.MenuItem(fmt.Sprint("Delete##delete_", idx), nil).
			Enabled(false),
		w.MenuItem(fmt.Sprint("Replace With Selected Object##replace_with_selected_", idx), nil).
			Enabled(false),
		w.MenuItem(fmt.Sprint("Reset to Default##reset_to_default_", idx), nil).Enabled(false),
	}
}

func getSprite(i *dmmprefab.Prefab) *dmicon.Sprite {
	return dmicon.Cache.GetSpriteOrPlaceholderV(
		i.Vars().TextV("icon", ""),
		i.Vars().TextV("icon_state", ""),
		i.Vars().IntV("dir", dm.DirDefault),
	)
}

func (t *TileMenu) iconSize() float32 {
	return 16 * t.app.PointSize()
}
