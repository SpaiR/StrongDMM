package tools

import (
	"sdmm/util"
)

type tSelect struct {
	tool

	editor editor
}

func (tSelect) Name() string {
	return TNSelect
}

func newSelect(editor editor) *tSelect {
	return &tSelect{
		editor: editor,
	}
}

func (tSelect) AltBehaviour() bool {
	return false
}

func (t tSelect) onStart(util.Point) {
	if hoveredInstance := t.editor.HoveredInstance(); hoveredInstance != nil {
		t.editor.SelectInstance(hoveredInstance)
	}
}
