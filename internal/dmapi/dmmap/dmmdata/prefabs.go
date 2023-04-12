package dmmdata

import (
	"sort"
	"strconv"
	"strings"

	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/util"
)

type Prefabs []*dmmprefab.Prefab

func (p Prefabs) Copy() Prefabs {
	cpy := make(Prefabs, len(p))
	copy(cpy, p)
	return cpy
}

func (p Prefabs) Equals(prefabs Prefabs) bool {
	if len(p) != len(prefabs) {
		return false
	}

	for idx, prefab := range p {
		if prefab.Id() != prefabs[idx].Id() {
			return false
		}
	}

	return true
}

func (p Prefabs) Hash() uint64 {
	sb := strings.Builder{}
	for _, prefab := range p {
		sb.WriteString(strconv.FormatUint(prefab.Id(), 10))
	}
	return util.Djb2(sb.String())
}

func (p Prefabs) Sorted() Prefabs {
	sorted := p.Copy()
	sort.SliceStable(sorted, func(i, j int) bool {
		return dm.PathWeight(sorted[i].Path()) < dm.PathWeight(sorted[j].Path())
	})
	return sorted
}
