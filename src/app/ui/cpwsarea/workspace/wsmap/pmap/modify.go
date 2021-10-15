package pmap

import (
	"sdmm/app/command"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/util"
)

func (p *PaneMap) UpdateCanvasByCoord(coord util.Point) {
	p.canvas.Render.UpdateBucket(p.dmm, p.activeLevel, []util.Point{coord})
}

func (p *PaneMap) SelectedInstance() (*dmmdata.Instance, bool) {
	return p.app.SelectedInstance()
}

func (p *PaneMap) CopyTiles() {
	p.app.Clipboard().Copy(p.dmm, []util.Point{p.canvasState.LastHoveredTile()})
}

func (p *PaneMap) PasteTiles() {
	p.app.Clipboard().Paste(p.dmm, p.canvasState.LastHoveredTile())
	p.CommitChanges("Paste")
}

func (p *PaneMap) CutTiles() {
	p.CopyTiles()
	p.DeleteTiles()
}

func (p *PaneMap) DeleteTiles() {
	tile := p.dmm.GetTile(p.canvasState.LastHoveredTile())

	for _, instance := range tile.Content() {
		if p.app.PathsFilter().IsVisiblePath(instance.Path()) {
			tile.RemoveByPath(instance.Path())
		}
	}

	tile.AdjustBaseContent()
	p.CommitChanges("Delete")
}

// CommitChanges triggers snapshot to commit changes and create a patch between two map states.
func (p *PaneMap) CommitChanges(changesType string) {
	stateId, tilesToUpdate := p.snapshot.Commit()

	// Do not push command if there is no tiles to update.
	if len(tilesToUpdate) == 0 {
		return
	}

	// Copy the value to pass it to the lambda.
	activeLevel := p.activeLevel

	// Ensure that the user has updated visuals.
	p.canvas.Render.UpdateBucket(p.dmm, activeLevel, tilesToUpdate)

	p.app.CommandStorage().Push(command.Make(changesType, func() {
		p.snapshot.GoTo(stateId - 1)
		p.canvas.Render.UpdateBucket(p.dmm, activeLevel, tilesToUpdate)
	}, func() {
		p.snapshot.GoTo(stateId)
		p.canvas.Render.UpdateBucket(p.dmm, activeLevel, tilesToUpdate)
	}))
}
