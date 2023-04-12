package editor

import (
	"sdmm/internal/app/command"
	"sdmm/internal/app/window"
	"sdmm/internal/util"
)

func (e *Editor) CommitMapSizeChange(oldMaxX, oldMaxY, oldMaxZ int) {
	initialMapTiles := e.pMap.Snapshot().Initial().Copy().Tiles // Remember initial tiles to restore them on undo.
	newMaxX, newMaxY, newMaxZ := e.dmm.MaxX, e.dmm.MaxY, e.dmm.MaxZ

	e.onMapSizeChange(e.dmm.MaxZ)

	e.app.CommandStorage().Push(command.Make("Set Map Size", func() {
		e.dmm.SetMapSize(oldMaxX, oldMaxY, oldMaxZ)
		e.dmm.Tiles = initialMapTiles
		e.onMapSizeChange(oldMaxZ)
	}, func() {
		e.dmm.SetMapSize(newMaxX, newMaxY, newMaxZ)
		e.onMapSizeChange(newMaxZ)
	}))
}

func (e *Editor) onMapSizeChange(maxZ int) {
	// Ensure we are on the visible level.
	if e.pMap.ActiveLevel() > maxZ {
		e.pMap.SetActiveLevel(maxZ)
	}
	e.pMap.Snapshot().Sync() // Do a full snapshots sync.
	e.pMap.OnMapSizeChange()
	e.updateAreasZones()
}

// CommitChanges triggers a snapshot to commit changes and create a patch between two map states.
func (e *Editor) CommitChanges(commitMsg string) {
	go e.commitChanges(commitMsg)
}

// Used as a wrapper to do a stuff inside the goroutine.
func (e *Editor) commitChanges(commitMsg string) {
	stateId, tilesToUpdate := e.pMap.Snapshot().Commit()

	// Do not push command if there is no tiles to update.
	if len(tilesToUpdate) == 0 {
		return
	}

	// Copy the value to pass it to the lambda.
	activeLevel := e.pMap.ActiveLevel()

	// Ensure that the user has updated visuals.
	e.updateAreasZones()
	e.updateBucket(activeLevel, tilesToUpdate)

	e.app.CommandStorage().Push(command.Make(commitMsg, func() {
		e.pMap.Snapshot().GoTo(stateId - 1)
		e.updateAreasZones()
		e.updateBucket(activeLevel, tilesToUpdate)
		e.dmm.PersistPrefabs()
		e.app.SyncPrefabs()
		e.app.SyncVarEditor()
	}, func() {
		e.pMap.Snapshot().GoTo(stateId)
		e.updateAreasZones()
		e.updateBucket(activeLevel, tilesToUpdate)
		e.dmm.PersistPrefabs()
		e.app.SyncPrefabs()
		e.app.SyncVarEditor()
	}))
}

// We need to update bucket in the main thread, since it can have OpenGL operations.
// RunLater do that by running the job in th end of the frame.
func (e *Editor) updateBucket(activeLevel int, tilesToUpdate []util.Point) {
	window.RunLater(func() {
		e.pMap.Canvas().Render().UpdateBucketV(e.dmm, activeLevel, tilesToUpdate)
	})
}
