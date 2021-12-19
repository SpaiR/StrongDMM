package tools

import (
	"sdmm/util"
)

type tPick struct {
	tool
}

func (tPick) Name() string {
	return TNPick
}

func newPick() *tPick {
	return &tPick{}
}

func (tPick) AltBehaviour() bool {
	return false
}

func (t tPick) onStart(util.Point) {
	if hoveredInstance := ed.HoveredInstance(); hoveredInstance != nil {
		ed.InstanceSelect(hoveredInstance)
	}
}
