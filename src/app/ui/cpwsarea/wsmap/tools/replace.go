package tools

import (
	"sdmm/util"
)

// ToolReplace can be used to replace the hovered instance with the selected prefab.
type ToolReplace struct {
	tool
}

func (ToolReplace) Name() string {
	return TNReplace
}

func newReplace() *ToolReplace {
	return &ToolReplace{}
}

func (ToolReplace) IgnoreBounds() bool {
	return true
}

func (ToolReplace) AltBehaviour() bool {
	return false
}

func (t ToolReplace) onStart(util.Point) {
	if hoveredInstance := ed.HoveredInstance(); hoveredInstance != nil {
		if selectedPrefab, ok := ed.SelectedPrefab(); ok {
			hoveredInstance.SetPrefab(selectedPrefab)
			ed.CommitChanges("Replace Instance")
		}
	}
}
