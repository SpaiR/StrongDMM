package dmmap

import (
	"sdmm/dm"
	"sdmm/dm/dmmap/dmmdata"
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

func (t *Tile) ContentAdd(instance *dmmdata.Instance) {
	t.content = append(t.content, instance)
}

func (t *Tile) ContentRemoveByPath(pathToRemove string) {
	var newContent dmmdata.Content
	for _, instance := range t.content {
		if !dm.IsPath(instance.Path(), pathToRemove) {
			newContent = append(newContent, instance)
		}
	}
	t.content = newContent
}

// ContentRegenerate adds missing base instances, if there are some of them.
func (t *Tile) ContentRegenerate() {
	var hasArea, hasTurf bool
	for _, instance := range t.content {
		if dm.IsPath(instance.Path(), "/area") {
			hasArea = true
		} else if dm.IsPath(instance.Path(), "/turf") {
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
