package tilemenu

import w "sdmm/imguiext/widget"

func (t *TileMenu) showControls() {
	w.Layout{
		w.MenuItem("Undo", t.app.DoUndo).
			Enabled(t.app.HasUndo()).
			Shortcut("Ctrl+Z"),
		w.MenuItem("Redo", t.app.DoRedo).
			Enabled(t.app.HasRedo()).
			Shortcut("Ctrl+Shift+Z"),
	}.Build()
}
