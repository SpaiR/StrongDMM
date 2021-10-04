package pmap

import (
	"sdmm/app/command"
	"sdmm/util"
)

// AddSelectedInstance adds currently selected instance on the map.
// If there is no selected instance, then nothing will happen.
func (p *PaneMap) AddSelectedInstance(coord util.Point) {
	if instance, ok := p.action.AppSelectedInstance(); ok {
		p.Dmm.GetTile(coord).Add(instance)
		p.canvas.Render.UpdateBucket(p.Dmm, p.activeLevel, []util.Point{coord})
	}
}

// HasSelectedInstance returns true if there is any selected instance.
func (p *PaneMap) HasSelectedInstance() bool {
	return p.action.AppHasSelectedInstance()
}

// CommitChanges triggers snapshot to commit changes and create a patch between two map states.
func (p *PaneMap) CommitChanges(changesType string) {
	stateId, tilesToUpdate := p.Snapshot.Commit()

	// Do not push command if there is no tiles to update.
	if len(tilesToUpdate) == 0 {
		return
	}

	// Copy the value to pass it to the lambda.
	activeLevel := p.activeLevel

	p.action.AppPushCommand(command.New(changesType, func() {
		p.Snapshot.GoTo(stateId - 1)
		p.canvas.Render.UpdateBucket(p.Dmm, activeLevel, tilesToUpdate)
	}, func() {
		p.Snapshot.GoTo(stateId)
		p.canvas.Render.UpdateBucket(p.Dmm, activeLevel, tilesToUpdate)
	}))
}
