package pmap

import (
	"sdmm/app/command"
	"sdmm/util"
)

// PMapAddSelectedInstance adds currently selected instance on the map.
// If there is no selected instance, then nothing will happen.
func (p *PaneMap) PMapAddSelectedInstance(coord util.Point) {
	if instance := p.action.AppSelectedInstance(); instance != nil {
		tile := p.Dmm.GetTile(coord)
		tile.Content = append(tile.Content, instance)
		p.canvas.Render.UpdateBucketV(p.Dmm, p.activeZLevel, []util.Point{coord})
	}
}

func (p *PaneMap) PMapHasSelectedInstance() bool {
	return p.action.AppHasSelectedInstance()
}

// PMapCommitChanges triggers snapshot to commit changes and create a patch between two map states.
func (p *PaneMap) PMapCommitChanges(changesType string) {
	stateId, tilesToUpdate := p.Snapshot.Commit()

	// Do not push command if there is no tiles to update.
	if len(tilesToUpdate) == 0 {
		return
	}

	// Copy the value to pass it to the lambda.
	activeZLevel := p.activeZLevel

	p.action.AppPushCommand(command.New(changesType, func() {
		p.Snapshot.GoTo(stateId - 1)
		p.canvas.Render.UpdateBucketV(p.Dmm, activeZLevel, tilesToUpdate)
	}, func() {
		p.Snapshot.GoTo(stateId)
		p.canvas.Render.UpdateBucketV(p.Dmm, activeZLevel, tilesToUpdate)
	}))
}

// PMapActiveZLevel returns current active Z-level.
func (p *PaneMap) PMapActiveZLevel() int {
	return p.activeZLevel
}
