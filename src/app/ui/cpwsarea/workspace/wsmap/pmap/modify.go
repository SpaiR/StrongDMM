package pmap

import (
	"sdmm/app/command"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/util"
)

// UpdateCanvasByCoord updates the canvas for the provided point.
func (p *PaneMap) UpdateCanvasByCoord(coord util.Point) {
	p.canvas.Render.UpdateBucket(p.dmm, p.activeLevel, []util.Point{coord})
}

// SelectedPrefab returns a currently selected prefab.
func (p *PaneMap) SelectedPrefab() (*dmmprefab.Prefab, bool) {
	return p.app.SelectedPrefab()
}

// CopyHoveredTile copies currently hovered tiles.
func (p *PaneMap) CopyHoveredTile() {
	p.app.Clipboard().Copy(p.dmm, []util.Point{p.canvasState.LastHoveredTile()})
}

// PasteHoveredTile does a paste to the currently hovered tile.
func (p *PaneMap) PasteHoveredTile() {
	p.app.Clipboard().Paste(p.dmm, p.canvasState.LastHoveredTile())
	go p.CommitChanges("Paste")
}

// CutHoveredTile does a cut (copy+delete) of the currently hovered tile.
func (p *PaneMap) CutHoveredTile() {
	p.CopyHoveredTile()
	p.DeleteHoveredTile()
}

// DeleteHoveredTile deletes the last hovered by the mouse tile.
func (p *PaneMap) DeleteHoveredTile() {
	tile := p.dmm.GetTile(p.canvasState.LastHoveredTile())

	for _, instance := range tile.Instances() {
		if p.app.PathsFilter().IsVisiblePath(instance.Prefab().Path()) {
			tile.InstancesRemoveByPath(instance.Prefab().Path())
		}
	}

	tile.InstancesRegenerate()
	go p.CommitChanges("Delete Tile")
}

// ReplacePrefab replaces all old prefabs on the map with the new one.
// Commits map changes.
func (p *PaneMap) ReplacePrefab(oldPrefab, newPrefab *dmmprefab.Prefab) {
	for _, tile := range p.dmm.Tiles {
		for _, instance := range tile.Instances() {
			if instance.Prefab().Id() == oldPrefab.Id() {
				instance.SetPrefab(newPrefab)
			}
		}
	}
	go p.CommitChanges("Replace Prefab")
}

// CommitChanges triggers a snapshot to commit changes and create a patch between two map states.
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
