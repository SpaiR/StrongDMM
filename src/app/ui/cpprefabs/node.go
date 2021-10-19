package cpprefabs

import (
	"sort"
	"strings"

	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
)

type prefabNode struct {
	name   string
	orig   *dmmprefab.Prefab
	sprite *dmicon.Sprite
}

func newPrefabNodes(prefabs dmmdata.Prefabs) []*prefabNode {
	nodes := make([]*prefabNode, 0, len(prefabs))
	for _, prefab := range prefabs {
		nodes = append(nodes, newPrefabNode(prefab))
	}

	if nodes != nil {
		// Group by icon_state
		sort.Slice(nodes, func(i, j int) bool {
			iIconState, _ := nodes[i].orig.Vars().Text("icon_state")
			jIconState, _ := nodes[j].orig.Vars().Text("icon_state")
			return strings.Compare(iIconState, jIconState) == -1
		})
		// Group by name
		sort.Slice(nodes, func(i, j int) bool {
			return strings.Compare(nodes[i].name, nodes[j].name) == -1
		})

		// Find the initial prefab index.
		idx := 0
		for i, node := range nodes {
			if node.orig.Vars().Len() == 0 {
				idx = i
				break
			}
		}

		// Move the initial prefab to the beginning of the slice
		initial := nodes[idx]
		nodes = append(nodes[:idx], nodes[idx+1:]...)
		nodes = append([]*prefabNode{initial}, nodes...)
	}

	return nodes
}

func newPrefabNode(prefab *dmmprefab.Prefab) *prefabNode {
	icon, _ := prefab.Vars().Text("icon")
	iconState, _ := prefab.Vars().Text("icon_state")
	dir, _ := prefab.Vars().Int("dir")
	return &prefabNode{
		name:   prefab.Path()[strings.LastIndex(prefab.Path(), "/")+1:],
		orig:   prefab,
		sprite: dmicon.Cache.GetSpriteOrPlaceholderV(icon, iconState, dir),
	}
}
