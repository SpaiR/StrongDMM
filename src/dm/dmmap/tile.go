package dmmap

import (
	"strconv"
	"strings"

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

func (t *Tile) Add(instance dmminstance.Instance) {
	t.content = append(t.content, instance)
}

func (t *Tile) Set(content TileContent) {
	t.content = content
}

type TileContent []dmminstance.Instance

func (t TileContent) Equals(content TileContent) bool {
	if len(t) != len(content) {
		return false
	}

	for idx, instance1 := range t {
		if instance1.Id() != content[idx].Id() {
			return false
		}
	}

	return true
}

func (t TileContent) Copy() TileContent {
	cpy := make(TileContent, len(t))
	copy(cpy, t)
	return cpy
}

func (t TileContent) Hash() uint64 {
	sb := strings.Builder{}
	for _, instance := range t {
		sb.WriteString(strconv.FormatUint(instance.Id(), 10))
	}
	return util.Djb2(sb.String())
}
