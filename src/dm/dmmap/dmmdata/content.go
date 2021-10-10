package dmmdata

import (
	"sort"
	"strconv"
	"strings"

	"sdmm/dm"
	"sdmm/util"
)

type Content []*Instance

func (c Content) Equals(content Content) bool {
	if len(c) != len(content) {
		return false
	}

	for idx, instance1 := range c {
		if instance1.Id() != content[idx].Id() {
			return false
		}
	}

	return true
}

func (c Content) Copy() Content {
	cpy := make(Content, len(c))
	copy(cpy, c)
	return cpy
}

func (c Content) Hash() uint64 {
	sb := strings.Builder{}
	for _, instance := range c {
		sb.WriteString(strconv.FormatUint(instance.Id(), 10))
	}
	return util.Djb2(sb.String())
}

func (c Content) Sorted() Content {
	sorted := c.Copy()
	sort.SliceStable(sorted, func(i, j int) bool {
		return dm.PathWeight(sorted[i].Path()) < dm.PathWeight(sorted[j].Path())
	})
	return sorted
}
