package tools

import (
	"sdmm/util"
)

type tPick struct {
	tool

	editor editor
}

func (tPick) Name() string {
	return TNPick
}

func newPick(editor editor) *tPick {
	return &tPick{
		editor: editor,
	}
}

func (tPick) AltBehaviour() bool {
	return false
}

func (t tPick) onStart(util.Point) {
	if hoveredInstance := t.editor.HoveredInstance(); hoveredInstance != nil {
		t.editor.SelectInstance(hoveredInstance)
	}
}
