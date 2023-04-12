package dmmsnap

import (
	"log"
	"strings"

	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmmdata"
	"sdmm/internal/util"
)

type dmmPatch []tilePatch

// DmmSnap is a structure to store map states in the different moments of time.
// It stores patches between different map states, so UNDO/REDO operations simplified to the "apply patch operation".
// TODO: Possible to implement local VCS history.
type DmmSnap struct {
	// Initial map is a state before changes. Basically, it's a copy of the current map, which is modified.
	// When we commit changes with Commit method we compare and collect differences between unmodifiable and
	// modifiable states. Those differences are collected into patches.
	initial, current *dmmap.Dmm

	stateId int

	patches []dmmPatch
}

func New(current *dmmap.Dmm) *DmmSnap {
	s := &DmmSnap{current: current}
	s.syncInitialWithCurrent()
	return s
}

func (d *DmmSnap) Sync() {
	d.syncInitialWithCurrent()
}

func (d *DmmSnap) Initial() *dmmap.Dmm {
	return d.initial
}

func (d *DmmSnap) Current() *dmmap.Dmm {
	return d.current
}

// Commit creates a patch with the map changes between two snapshot states.
// stateId is an integer value, which can be used in the future to iterate snapshot to the specific state.
func (d *DmmSnap) Commit() (int, []util.Point) {
	log.Println("[snapshot] committing snapshot state...")

	var tilePatches []tilePatch

	// Iterate through the current tiles state.
	for _, currentTile := range d.current.Tiles {
		initialTile := d.initial.GetTile(currentTile.Coord)

		currInstances := currentTile.Instances()
		initialInstances := initialTile.Instances()

		// If tiles contents have different length, then they are different for sure.
		tileModified := len(currInstances) != len(initialInstances)

		if !tileModified {
			tileModified = !currInstances.PrefabsEquals(initialInstances)
		}

		// No changes - no patch.
		if !tileModified {
			continue
		}

		tilePatches = append(tilePatches, tilePatch{
			coord:    currentTile.Coord,
			backward: initialInstances.Prefabs(),
			forward:  currInstances.Prefabs(),
		})
	}

	var tilesToUpdate []util.Point

	// Add new patches and sync map states.
	if len(tilePatches) != 0 {
		log.Println("[snapshot] collected snapshot patches count:", len(tilePatches))

		// Drop all patches, if their stateId is more than the current one.
		d.patches = append(d.patches[:d.stateId], tilePatches)
		// Apply created patch to the initial state, so it will be synced with the current one.
		d.patchState(d.stateId, true, patchInitial)
		// Update stateId to a new value.
		d.stateId = len(d.patches)

		// Collect tiles to update from created tile patches
		tilesToUpdate = make([]util.Point, 0, len(tilePatches))
		for _, patch := range tilePatches {
			tilesToUpdate = append(tilesToUpdate, patch.coord)
		}
	}

	log.Println("[snapshot] snapshot state committed")

	return d.stateId, tilesToUpdate
}

// GoTo will update DmmSnap state by applying patches.
func (d *DmmSnap) GoTo(stateId int) {
	log.Println("[snapshot] changing snapshot state to:", stateId)
	d.goTo(stateId, patchFull)
}

func (d *DmmSnap) goTo(stateId int, patchType patchType) {
	if d.stateId != stateId {
		isForward := d.stateId < stateId

		if !isForward {
			d.stateId--
		}
		d.patchState(d.stateId, isForward, patchType)
		if isForward {
			d.stateId++
		}

		d.goTo(stateId, patchType)
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

func (d *DmmSnap) patchState(stateId int, isForward bool, patchType patchType) {
	log.Printf("[snapshot] patching:[%d], forward:[%t], type:[%s]", stateId, isForward, patchType)
	for _, patch := range d.patches[stateId] {
		var prefabs dmmdata.Prefabs
		if isForward {
			prefabs = patch.forward
		} else {
			prefabs = patch.backward
		}

		// Update current map.
		if patchType&patchCurrent != 0 {
			d.current.GetTile(patch.coord).InstancesSet(prefabs)
		}

		// Update initial map.
		if patchType&patchInitial != 0 {
			d.initial.GetTile(patch.coord).InstancesSet(prefabs)
		}
	}
}

// Basically, does a full copy of the current state to the initial one.
func (d *DmmSnap) syncInitialWithCurrent() {
	log.Println("[snapshot] syncing initial state with the current...")

	d.initial = &dmmap.Dmm{
		Name: d.current.Name,
		Path: d.current.Path,
		MaxX: d.current.MaxX,
		MaxY: d.current.MaxY,
		MaxZ: d.current.MaxZ,
	}

	// Do a full copy of tiles from the current map state to the initial.
	d.initial.Tiles = make([]*dmmap.Tile, 0, len(d.current.Tiles))
	for _, tile := range d.current.Tiles {
		tileCopy := tile.Copy()
		d.initial.Tiles = append(d.initial.Tiles, &tileCopy)
	}

	log.Println("[snapshot] initial state synced with the current")
}

type tilePatch struct {
	coord util.Point

	backward dmmdata.Prefabs // State to restore.
	forward  dmmdata.Prefabs // State to reproduce.
}
