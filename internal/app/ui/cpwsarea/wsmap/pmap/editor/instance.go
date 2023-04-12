package editor

import (
	"sdmm/internal/app/ui/layout/lnode"
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/dmapi/dmmap/dmminstance"
)

// InstanceSelect selects the provided instance to edit.
func (e *Editor) InstanceSelect(i *dmminstance.Instance) {
	e.app.DoSelectPrefab(i.Prefab())
	e.app.ShowLayout(lnode.NamePrefabs, false)
	e.app.DoEditInstance(i)
	e.app.ShowLayout(lnode.NameVariables, false)
}

// InstanceMoveToTop swaps the provided instance with the one which is upper.
func (e *Editor) InstanceMoveToTop(i *dmminstance.Instance) {
	e.instanceMove(e.dmm.GetTile(i.Coord()), i, true)
}

// InstanceMoveToBottom swaps the provided instance with the one which is under.
func (e *Editor) InstanceMoveToBottom(i *dmminstance.Instance) {
	e.instanceMove(e.dmm.GetTile(i.Coord()), i, false)
}

func (e *Editor) instanceMove(tile *dmmap.Tile, i *dmminstance.Instance, top bool) {
	sortedInstances := tile.Instances().Sorted()

	for idx, instance := range sortedInstances {
		if instance.Id() == i.Id() {
			var nextIdx int
			if top {
				nextIdx = idx + 1
			} else {
				nextIdx = idx - 1
			}

			if nextIdx < 0 || nextIdx >= len(sortedInstances) {
				break
			}

			nextInstance := sortedInstances[nextIdx]
			nextInstancePath := nextInstance.Prefab().Path()

			// Move the instance only if the next instance is /obj or /mob type.
			if dm.IsPath(nextInstancePath, "/obj") || dm.IsPath(nextInstancePath, "/mob") {
				sortedInstances[idx] = sortedInstances[nextIdx]
				sortedInstances[nextIdx] = instance

				tile.Set(sortedInstances)
			}

			return
		}
	}
}

// InstanceDelete deletes the provided instance from the map.
func (e *Editor) InstanceDelete(i *dmminstance.Instance) {
	tile := e.dmm.GetTile(i.Coord())
	tile.InstancesRemoveByInstance(i)
	tile.InstancesRegenerate()
}

// InstancesDeleteByPrefab deletes from the map all instances from the provided prefab.
func (e *Editor) InstancesDeleteByPrefab(prefab *dmmprefab.Prefab) {
	instances := e.InstancesFindByPrefabId(prefab.Id())
	for _, instance := range instances {
		tile := e.dmm.GetTile(instance.Coord())
		tile.InstancesRemoveByInstance(instance)
		tile.InstancesRegenerate()
	}
}

// InstanceReplace replaces the provided instance with the provided prefab.
func (e *Editor) InstanceReplace(i *dmminstance.Instance, prefab *dmmprefab.Prefab) {
	tile := e.dmm.GetTile(i.Coord())

	instances := tile.Instances()
	for _, instance := range instances {
		if instance.Id() == i.Id() {
			if dm.IsPathBaseSame(instance.Prefab().Path(), prefab.Path()) {
				instance.SetPrefab(prefab)
			}
			return
		}
	}
}

// InstanceReset resets the provided instance to the initial state (no custom variables).
func (e *Editor) InstanceReset(i *dmminstance.Instance) {
	i.SetPrefab(dmmap.PrefabStorage.Initial(i.Prefab().Path()))
}

// InstancesFindByPrefabId returns all instances from the current map with a corresponding prefab ID.
func (e *Editor) InstancesFindByPrefabId(prefabId uint64) (result []*dmminstance.Instance) {
	for _, tile := range e.dmm.Tiles {
		for _, instance := range tile.Instances() {
			if instance.Prefab().Id() == prefabId {
				result = append(result, instance)
			}
		}
	}
	return result
}
