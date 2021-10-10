package snapshot

import (
	"log"
	"strings"

	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/util"
)

type dmmPatch []tilePatch

// Snapshot is a structure to store map states in the different moments of time.
// It stores patches between different map states, so UNDO/REDO operations simplified to the "apply patch operation".
// TODO: Possible to implement local VCS history.
type Snapshot struct {
	// Initial map is a state before changes. Basically, it's a copy of the current map, which is modified.
	// When we commit changes with Commit method we compare and collect differences between unmodifiable and
	// modifiable states. Those differences are collected into patches.
	initial, current *dmmap.Dmm

	stateId int

	patches []dmmPatch
}

func NewSnapshot(current *dmmap.Dmm) *Snapshot {
	s := &Snapshot{current: current}
	s.syncInitialWithCurrent()
	return s
}

// Commit creates a patch with the map changes between two snapshot states.
// stateId is an integer value, which can be used in the future to iterate snapshot to the specific state.
func (s *Snapshot) Commit() (int, []util.Point) {
	log.Println("[snapshot] committing snapshot state...")

	var tilePatches []tilePatch

	// Iterate through the current tiles state.
	for _, currentTile := range s.current.Tiles {
		initialTile := s.initial.GetTile(currentTile.Coord)

		// If tiles contents have different length, then they are different for sure.
		tileModified := len(currentTile.Content()) != len(initialTile.Content())

		if !tileModified {
			tileModified = !currentTile.Content().Equals(initialTile.Content())
		}

		// No changes - no patch.
		if !tileModified {
			continue
		}

		tilePatches = append(tilePatches, tilePatch{
			coord:    currentTile.Coord,
			backward: initialTile.Content().Copy(),
			forward:  currentTile.Content().Copy(),
		})
	}

	var tilesToUpdate []util.Point

	// Add new patches and sync map states.
	if len(tilePatches) != 0 {
		log.Println("[snapshot] collected snapshot patches count:", len(tilePatches))

		// Drop all patches, if their stateId is more than the current one.
		s.patches = append(s.patches[:s.stateId], tilePatches)
		// Apply created patch to the initial state, so it will be synced with the current one.
		s.patchState(s.stateId, true, patchInitial)
		// Update stateId to a new value.
		s.stateId = len(s.patches)

		// Collect tiles to update from created tile patches
		tilesToUpdate = make([]util.Point, 0, len(tilePatches))
		for _, patch := range tilePatches {
			tilesToUpdate = append(tilesToUpdate, patch.coord)
		}
	}

	log.Println("[snapshot] snapshot state committed")

	return s.stateId, tilesToUpdate
}

// GoTo will update Snapshot state by applying spe
func (s *Snapshot) GoTo(stateId int) {
	log.Println("[snapshot] changing snapshot state to:", stateId)
	s.goTo(stateId, patchFull)
}

func (s *Snapshot) goTo(stateId int, patchType patchType) {
	if s.stateId != stateId {
		isForward := s.stateId < stateId

		if !isForward {
			s.stateId--
		}
		s.patchState(s.stateId, isForward, patchType)
		if isForward {
			s.stateId++
		}

		s.goTo(stateId, patchType)
	}
}

type patchType int

const (
	patchInitial patchType = 1
	patchCurrent patchType = 2
	patchFull              = patchInitial | patchCurrent
)

func (p patchType) String() string {
	var results []string
	if p&patchInitial != 0 {
		results = append(results, "patchInitial")
	}
	if p&patchCurrent != 0 {
		results = append(results, "patchCurrent")
	}
	return strings.Join(results, "|")
}

func (s *Snapshot) patchState(stateId int, isForward bool, patchType patchType) {
	log.Printf("[snapshot] patching:[%d], forward:[%t], type:[%s]", stateId, isForward, patchType)
	for _, patch := range s.patches[stateId] {
		var content dmmdata.Content
		if isForward {
			content = patch.forward
		} else {
			content = patch.backward
		}

		// Update current map.
		if patchType&patchCurrent != 0 {
			s.current.GetTile(patch.coord).Set(content.Copy())
		}

		// Update initial map.
		if patchType&patchInitial != 0 {
			s.initial.GetTile(patch.coord).Set(content.Copy())
		}
	}
}

// Basically, does a full copy of the current state to the initial one.
func (s *Snapshot) syncInitialWithCurrent() {
	log.Println("[snapshot] syncing initial state with the current...")

	s.initial = &dmmap.Dmm{
		Name: s.current.Name,
		Path: s.current.Path,
		MaxX: s.current.MaxX,
		MaxY: s.current.MaxY,
		MaxZ: s.current.MaxZ,
	}

	// Do a full copy of tiles from the current map state to the initial.
	s.initial.Tiles = make([]*dmmap.Tile, 0, len(s.current.Tiles))
	for _, tile := range s.current.Tiles {
		tileCopy := tile.Copy()
		s.initial.Tiles = append(s.initial.Tiles, &tileCopy)
	}

	log.Println("[snapshot] initial state synced with the current")
}

type tilePatch struct {
	coord util.Point

	backward dmmdata.Content // State to restore.
	forward  dmmdata.Content // State to reproduce.
}
