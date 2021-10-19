package dmmdata

import (
	"strconv"
	"strings"

	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/util"
)

type Prefabs []*dmmprefab.Prefab

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
