package cpprefabs

import (
	"sort"
	"strings"

	"github.com/SpaiR/imgui-go"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmvars"
	"sdmm/util"
)

type prefabNode struct {
	name   string
	orig   *dmmprefab.Prefab
	sprite *dmicon.Sprite
	color  imgui.Vec4
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
		idx := -1
		for i, node := range nodes {
			if node.orig.Vars().Len() == 0 {
				idx = i
				break
			}
		}

		if idx == -1 {
			// If the initial prefab index is still -1, then we don't have it.  We will add the one.
			initialPrefab := dmmap.PrefabStorage.Initial(prefabs[0].Path())
			nodes = append([]*prefabNode{newPrefabNode(initialPrefab)}, nodes...)
		} else {
			// Move the initial prefab to the beginning of the slice
			initial := nodes[idx]
			nodes = append(nodes[:idx], nodes[idx+1:]...)
			nodes = append([]*prefabNode{initial}, nodes...)
		}
	}

	return nodes
}

func newPrefabNode(prefab *dmmprefab.Prefab) *prefabNode {
	name := prefab.Vars().TextV("name", dm.PathLast(prefab.Path()))
	icon, _ := prefab.Vars().Text("icon")
	iconState, _ := prefab.Vars().Text("icon_state")
	dir, _ := prefab.Vars().Int("dir")
	r, g, b, _ := util.ParseColor(prefab.Vars().TextV("color", dmvars.NullValue)).RGBA()
	return &prefabNode{
		name:   name,
		orig:   prefab,
		sprite: dmicon.Cache.GetSpriteOrPlaceholderV(icon, iconState, dir),
		color:  imgui.Vec4{X: r, Y: g, Z: b, W: 1},
	}
}
