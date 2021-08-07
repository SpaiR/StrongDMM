package dmmap

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
)

type dmmPatch []tilePatch

// Snapshot is a structure to store map states in the different moments of time.
// It stores patches between different map states, so UNDO/REDO operations simplified to the "apply patch operation".
// TODO: provide local VCS
type Snapshot struct {
	initial, current *Dmm

	stateId int

	patches []dmmPatch
}

func NewSnapshot(current *Dmm) *Snapshot {
	s := &Snapshot{current: current}
	s.sync()
	return s
}

// Commit creates a patch with the map changes between two snapshot states.
// stateId is an integer value, which can be used in the future to iterate snapshot to the specific state.
func (s *Snapshot) Commit() (stateId int) {
	log.Println("[dmmap] committing snapshot state...")

	var tilePatches []tilePatch

	// Iterate through the current tiles state.
	for _, currentTile := range s.current.Tiles {
		initialTile := s.initial.GetTile(currentTile.X, currentTile.Y, currentTile.Z)

		// If tiles contents have different length, then they are different for sure.
		tileModified := len(currentTile.Content) != len(initialTile.Content)

		if !tileModified {
			// Iteratee through tiles contents and compare instances inside them.
			for instanceIdx, currentInstance := range currentTile.Content {
				if tileModified = currentInstance.Id != initialTile.Content[instanceIdx].Id; tileModified {
					break
				}
			}
		}

		// No changes - no patch.
		if !tileModified {
			continue
		}

		// State to restore.
		backward := make([]*dmminstance.Instance, len(initialTile.Content))
		copy(backward, initialTile.Content)

		// State to reproduce.
		forward := make([]*dmminstance.Instance, len(currentTile.Content))
		copy(forward, currentTile.Content)

		tilePatches = append(tilePatches, tilePatch{
			x:        currentTile.X,
			y:        currentTile.Y,
			z:        currentTile.Z,
			backward: backward,
			forward:  forward,
		})
	}

	// Add new patches and sync map states.
	if len(tilePatches) != 0 {
		log.Println("[dmmap] collected snapshot patches count:", len(tilePatches))

		oldPatchesLen := len(s.patches)
		s.patches = append(s.patches[:s.stateId], tilePatches) // Drop all patches, if their stateId is more than the current one.
		newPatchesLen := len(s.patches)

		if oldPatchesLen > newPatchesLen {
			log.Println("[dmmap] dropped patches count:", oldPatchesLen-newPatchesLen)
		} else {
			log.Println("[dmmap] added patches count:", newPatchesLen-oldPatchesLen)
		}

		s.stateId = len(s.patches)
		s.sync()
	}

	log.Println("[dmmap] snapshot state committed")

	return s.stateId
}

// GoTo used to change current map state, so it will be equal to a specific stateId value.
func (s *Snapshot) GoTo(stateId int) {
	log.Println("[dmmap] changing snapshot state to:", stateId)
	s.goTo(stateId)
	s.sync() // Call sync after we've reached the state we want.
}

func (s *Snapshot) goTo(stateId int) {
	if s.stateId != stateId {
		s.patchState(s.stateId < stateId)
		s.goTo(stateId)
	}
}

func (s *Snapshot) patchState(isForward bool) {
	if !isForward {
		s.stateId--
	}

	for _, patch := range s.patches[s.stateId] {
		var content []*dmminstance.Instance
		if isForward {
			content = patch.forward
		} else {
			content = patch.backward
		}

		tile := s.current.GetTile(patch.x, patch.y, patch.z)
		tile.Content = make([]*dmminstance.Instance, len(content))
		copy(tile.Content, content)
	}

	if isForward {
		s.stateId++
	}
}

// sync do a synchronization between Snapshot states.
// Need to be called after any state update to ensure other patches will be created properly.
func (s *Snapshot) sync() {
	log.Println("[dmmap] syncing snapshot state...")

	s.initial = &Dmm{
		Name: s.current.Name,
		Path: s.current.Path,
		MaxX: s.current.MaxX,
		MaxY: s.current.MaxY,
		MaxZ: s.current.MaxZ,
	}

	// Do a full copy of tiles from the current map state to the initial.
	for _, tile := range s.current.Tiles {
		tileCopy := tile.Copy()
		s.initial.Tiles = append(s.initial.Tiles, &tileCopy)
	}

	log.Println("[dmmap] snapshot state synced")
}

type tilePatch struct {
	x, y, z int

	backward, forward []*dmminstance.Instance
}
