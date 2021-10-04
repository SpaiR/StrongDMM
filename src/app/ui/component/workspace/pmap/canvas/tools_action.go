package canvas

import "sdmm/util"

func (t *Tools) ToolsAddModifiedTile(coord util.Point) {
	t.state.AddModifiedTile(coord)
}

func (t *Tools) ToolsResetModifiedTiles() {
	t.state.ResetModifiedTiles()
}
