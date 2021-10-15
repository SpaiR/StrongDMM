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
	}.Build()
}
