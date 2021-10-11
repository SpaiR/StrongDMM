package tilemenu

import w "sdmm/imguiext/widget"

func (t *TileMenu) showControls() {
	w.Layout{
		w.MenuItem("Undo", t.action.AppDoUndo).
			Enabled(t.action.AppHasUndo()).
			Shortcut("Ctrl+Z"),
		w.MenuItem("Redo", t.action.AppDoRedo).
			Enabled(t.action.AppHasRedo()).
			Shortcut("Ctrl+Shift+Z"),
	}.Build()
}
