package dmmap

import (
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

type Tile struct {
	Coord   util.Point
	content dmmdata.Content
}

func (t Tile) Copy() Tile {
	return Tile{
		t.Coord,
		t.content.Copy(),
	}
}

func (t Tile) Content() dmmdata.Content {
	return t.content
}

func (t *Tile) ContentSet(content dmmdata.Content) {
	t.content = content
}

func (t *Tile) ContentAdd(prefab *dmmdata.Prefab) {
	t.content = append(t.content, prefab)
}

func (t *Tile) ContentRemoveByPath(pathToRemove string) {
	var newContent dmmdata.Content
	for _, prefab := range t.content {
		if !dm.IsPath(prefab.Path(), pathToRemove) {
			newContent = append(newContent, prefab)
		}
	}
	t.content = newContent
}

// ContentRegenerate adds missing base prefabs, if there are some of them.
func (t *Tile) ContentRegenerate() {
	var hasArea, hasTurf bool
	for _, prefab := range t.content {
		if dm.IsPath(prefab.Path(), "/area") {
			hasArea = true
		} else if dm.IsPath(prefab.Path(), "/turf") {
			hasTurf = true
		}
	}
	if !hasArea {
		t.ContentAdd(baseArea)
	}
	if !hasTurf {
		t.ContentAdd(baseTurf)
	}
}
