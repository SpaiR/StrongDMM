package tilemenu

import w "sdmm/imguiext/widget"

func (t *TileMenu) showControls() {
	w.Layout{
		w.MenuItem("Undo", t.app.DoUndo).
			Enabled(t.app.CommandStorage().HasUndo()).
			Shortcut("Ctrl+Z"),
		w.MenuItem("Redo", t.app.DoRedo).
			Enabled(t.app.CommandStorage().HasRedo()).
			Shortcut("Ctrl+Shift+Z"),
	}.Build()
}
