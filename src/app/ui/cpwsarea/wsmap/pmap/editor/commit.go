package editor

import "sdmm/app/command"

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
	e.pMap.Canvas().Render().UpdateBucketV(e.dmm, activeLevel, tilesToUpdate)

	e.app.CommandStorage().Push(command.Make(commitMsg, func() {
		e.pMap.Snapshot().GoTo(stateId - 1)
		e.updateAreasZones()
		e.pMap.Canvas().Render().UpdateBucketV(e.dmm, activeLevel, tilesToUpdate)
		e.dmm.PersistPrefabs()
		e.app.SyncPrefabs()
		e.app.SyncVarEditor()
	}, func() {
		e.pMap.Snapshot().GoTo(stateId)
		e.updateAreasZones()
		e.pMap.Canvas().Render().UpdateBucketV(e.dmm, activeLevel, tilesToUpdate)
		e.dmm.PersistPrefabs()
		e.app.SyncPrefabs()
		e.app.SyncVarEditor()
	}))
}
