package pmap

import (
	"sdmm/app/command"
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

func (p *PaneMap) UpdateCanvasByCoord(coord util.Point) {
	p.canvas.Render.UpdateBucket(p.dmm, p.activeLevel, []util.Point{coord})
}

func (p *PaneMap) SelectedInstance() (dmminstance.Instance, bool) {
	return p.action.AppSelectedInstance()
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

	p.action.AppPushCommand(command.New(changesType, func() {
		p.snapshot.GoTo(stateId - 1)
		p.canvas.Render.UpdateBucket(p.dmm, activeLevel, tilesToUpdate)
	}, func() {
		p.snapshot.GoTo(stateId)
		p.canvas.Render.UpdateBucket(p.dmm, activeLevel, tilesToUpdate)
	}))
}
