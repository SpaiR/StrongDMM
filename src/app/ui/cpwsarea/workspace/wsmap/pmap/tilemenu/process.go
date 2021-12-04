package tilemenu

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/imguiext"
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
			Icon(imguiext.IconFaUndo).
			Enabled(t.app.CommandStorage().HasUndo()).
			Shortcut("Ctrl+Z"),
		w.MenuItem("Redo", t.app.DoRedo).
			Icon(imguiext.IconFaRedo).
			Enabled(t.app.CommandStorage().HasRedo()).
			Shortcut("Ctrl+Shift+Z"),
		w.Separator(),
		w.MenuItem("Copy", t.app.DoCopy).
			Icon(imguiext.IconFaCopy).
			Shortcut("Ctrl+C"),
		w.MenuItem("Paste", t.app.DoPaste).
			Icon(imguiext.IconFaPaste).
			Enabled(t.app.Clipboard().HasData()).
			Shortcut("Ctrl+V"),
		w.MenuItem("Cut", t.app.DoCut).
			Icon(imguiext.IconFaCut).
			Shortcut("Ctrl+X"),
		w.MenuItem("Delete", t.app.DoDelete).
			Icon(imguiext.IconFaEraser).
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
		w.Menu(name, t.showInstanceControls(i, idx)).
			IconEmpty(),
		w.Custom(func() {
			// Draw the instance icon.
			imgui.WindowDrawList().AddImageV(
				imgui.TextureID(s.Texture()),
				imgui.ItemRectMin(),
				imgui.ItemRectMin().Plus(imgui.Vec2{X: iconSize, Y: iconSize}),
				imgui.Vec2{X: s.U1, Y: s.V1}, imgui.Vec2{X: s.U2, Y: s.V2},
				imgui.PackedColorFromVec4(imgui.Vec4{X: r, Y: g, Z: b, W: 1}),
			)
		}),
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
					w.MenuItem(fmt.Sprint("Move to Top##move_to_top_", idx), t.doMoveToTop(i)).
						Icon(imguiext.IconFaArrowUp),
					w.MenuItem(fmt.Sprint("Move to Bottom##move_to_bottom_", idx), t.doMoveToBottom(i)).
						Icon(imguiext.IconFaArrowDown),
					w.Separator(),
				}.Build()
			}
		}),
		w.MenuItem(fmt.Sprint("Select##select_", idx), t.doSelect(i)).
			Icon(imguiext.IconFaEyeDropper).
			Shortcut("Shift+LMB"),
		w.MenuItem(fmt.Sprint("Delete##delete_", idx), t.doDelete(i)).
			Icon(imguiext.IconFaEraser),
		w.MenuItem(fmt.Sprint("Replace With Selected##replace_with_selected_", idx), t.doReplaceWithSelected(i)).
			IconEmpty().
			Enabled(t.app.HasSelectedPrefab()),
		w.MenuItem(fmt.Sprint("Reset to Default##reset_to_default_", idx), t.doResetToDefault(i)).
			IconEmpty(),
	}
}

func (t *TileMenu) doMoveToTop(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do move instance[%s] to top: %d", i.Prefab().Path(), i.Id())
		t.editor.MoveInstanceToTop(i)
	}
}

func (t *TileMenu) doMoveToBottom(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do move instance[%s] to bottom: %d", i.Prefab().Path(), i.Id())
		t.editor.MoveInstanceToBottom(i)
	}
}

func (t *TileMenu) doSelect(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do select instance[%s]: %d", i.Prefab().Path(), i.Id())
		t.editor.SelectInstance(i)
	}
}

func (t *TileMenu) doDelete(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do delete instance[%s]: %d", i.Prefab().Path(), i.Id())
		t.editor.DeleteInstance(i)
	}
}

func (t *TileMenu) doReplaceWithSelected(i *dmminstance.Instance) func() {
	return func() {
		if prefab, ok := t.app.SelectedPrefab(); ok {
			log.Printf("[tilemenu] do replace instance[%s] with [%s]: %d", i.Prefab().Path(), prefab.Path(), i.Id())
			t.editor.ReplaceInstance(i, prefab)
		}
	}
}

func (t *TileMenu) doResetToDefault(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do reset instance[%s] to default: %d", i.Prefab().Path(), i.Id())
		t.editor.ResetInstance(i)
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
