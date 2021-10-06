package dmmap

import (
	"sdmm/dm"
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

type Tile struct {
	Coord   util.Point
	content TileContent
}

func (t Tile) Content() TileContent {
	return t.content
}

func (t Tile) Copy() Tile {
	return Tile{
		t.Coord,
		t.content.Copy(),
	}
}

func (t *Tile) AddInstance(instance dmminstance.Instance) {
	t.content = append(t.content, instance)
}

func (t *Tile) RemoveInstancesByPath(pathToRemove string) {
	var newContent TileContent
	for _, instance := range t.content {
		if !dm.IsPath(instance.Path, pathToRemove) {
			newContent = append(newContent, instance)
		}
	}
	t.content = newContent
}

func (t *Tile) Set(content TileContent) {
	t.content = content
}
