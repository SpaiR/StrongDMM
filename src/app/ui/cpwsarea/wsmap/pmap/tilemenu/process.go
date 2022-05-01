package tilemenu

import (
	"fmt"
	"log"

	"sdmm/app/ui/shortcut"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/imguiext/icon"
	w "sdmm/imguiext/widget"
	"sdmm/util"

	"github.com/SpaiR/imgui-go"
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
	if !imgui.IsWindowHovered() && imgui.IsMouseClicked(imgui.MouseButtonMiddle) {
		t.close()
	}
}

func (t *TileMenu) showControls() {
	w.Layout{
		w.MenuItem(t.tile.Coord.String(), nil).Icon(icon.Help).Enabled(false),
		w.Separator(),
		w.MenuItem("Undo", t.app.DoUndo).
			Icon(icon.Undo).
			Enabled(t.app.CommandStorage().HasUndo()).
			Shortcut(shortcut.KeyModName(), "Z"),
		w.MenuItem("Redo", t.app.DoRedo).
			Icon(icon.Redo).
			Enabled(t.app.CommandStorage().HasRedo()).
			Shortcut(shortcut.KeyModName(), "Shift", "Z"),
		w.Separator(),
		w.MenuItem("Copy", t.app.DoCopy).
			Icon(icon.ContentCopy).
			Shortcut(shortcut.KeyModName(), "C"),
		w.MenuItem("Paste", t.app.DoPaste).
			Icon(icon.ContentPaste).
			Enabled(t.app.Clipboard().HasData()).
			Shortcut(shortcut.KeyModName(), "V"),
		w.MenuItem("Cut", t.app.DoCut).
			Icon(icon.ContentCut).
			Shortcut(shortcut.KeyModName(), "X"),
		w.MenuItem("Delete", t.app.DoDelete).
			Icon(icon.Eraser).
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
			if t.app.Prefs().Controls.QuickEditContextMenu {
				t.pQuickEdit.ProcessV(i)
				imgui.Separator()
			}
		}),
		w.Custom(func() {
			if dm.IsPath(p.Path(), "/obj") || dm.IsPath(p.Path(), "/mob") {
				w.Layout{
					w.MenuItem(fmt.Sprint("Move to Top##move_to_top_", idx), t.doMoveToTop(i)).
						Icon(icon.ArrowUpward),
					w.MenuItem(fmt.Sprint("Move to Bottom##move_to_bottom_", idx), t.doMoveToBottom(i)).
						Icon(icon.ArrowDownward),
					w.Separator(),
				}.Build()
			}
		}),
		w.MenuItem(fmt.Sprint("Select##select_", idx), t.doSelect(i)).
			Icon(icon.EyeDropper).
			Shortcut("S"),
		w.MenuItem(fmt.Sprint("Delete##delete_", idx), t.doDelete(i)).
			Icon(icon.Eraser).
			Shortcut("D"),
		w.MenuItem(fmt.Sprint("Replace##replace_", idx), t.doReplace(i)).
			Icon(icon.Repeat).
			Shortcut("R").
			Enabled(t.app.HasSelectedPrefab()),
		w.MenuItem(fmt.Sprint("Reset to Default##reset_to_default_", idx), t.doResetToDefault(i)).
			IconEmpty(),
	}
}

func (t *TileMenu) doMoveToTop(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do move instance[%s] to top: %d", i.Prefab().Path(), i.Id())
		t.editor.InstanceMoveToTop(i)
		t.editor.CommitChanges("Move to Top")
	}
}

func (t *TileMenu) doMoveToBottom(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do move instance[%s] to bottom: %d", i.Prefab().Path(), i.Id())
		t.editor.InstanceMoveToBottom(i)
		t.editor.CommitChanges("Move to Bottom")
	}
}

func (t *TileMenu) doSelect(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do select instance[%s]: %d", i.Prefab().Path(), i.Id())
		t.editor.InstanceSelect(i)
	}
}

func (t *TileMenu) doDelete(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do delete instance[%s]: %d", i.Prefab().Path(), i.Id())
		t.editor.InstanceDelete(i)
		t.editor.CommitChanges("Delete Instance")
	}
}

func (t *TileMenu) doReplace(i *dmminstance.Instance) func() {
	return func() {
		if prefab, ok := t.app.SelectedPrefab(); ok {
			log.Printf("[tilemenu] do replace instance[%s] with [%s]: %d", i.Prefab().Path(), prefab.Path(), i.Id())
			t.editor.InstanceReplace(i, prefab)
			t.editor.CommitChanges("Replace Instance")
		}
	}
}

func (t *TileMenu) doResetToDefault(i *dmminstance.Instance) func() {
	return func() {
		log.Printf("[tilemenu] do reset instance[%s] to default: %d", i.Prefab().Path(), i.Id())
		t.editor.InstanceReset(i)
		t.editor.CommitChanges("Reset Instance")
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
	return imgui.FrameHeight() - imgui.CurrentStyle().FramePadding().Y
}
