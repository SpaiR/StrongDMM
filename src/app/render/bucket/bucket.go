package bucket

import (
	"log"
	"sort"

	"sdmm/dm/dmmap"
	"sdmm/util"
)

// Bucket contains data needed to render the map.
// The Bucket itself is made of Level's which are made of Chunk's.
type Bucket struct {
	Levels []int
	levels map[int]*Level
}

func New() *Bucket {
	return &Bucket{
		levels: make(map[int]*Level),
	}
}

// UpdateLevel updates a specific level of the bucket. If the level not exist, will create it at first.
func (b *Bucket) UpdateLevel(dmm *dmmap.Dmm, level int, tilesToUpdate []util.Point) {
	log.Printf("[bucket] updating bucket with [%s]...", dmm.Path.Readable)
	b.getOrCreateLevel(dmm, level).update(dmm, tilesToUpdate)
	log.Println("[bucket] bucket updated")
}

// Level returns a specific level of the bucket or nil if it's not exist.
func (b *Bucket) Level(level int) *Level {
	return b.levels[level]
}

func (b *Bucket) getOrCreateLevel(dmm *dmmap.Dmm, level int) *Level {
	if l, ok := b.levels[level]; ok {
		return l
	} else {
		log.Println("[bucket] created level:", level)
		l = newLevel(dmm, level)
		b.Levels = append(b.Levels, level)
		sort.Ints(b.Levels)
		b.levels[level] = l
		return l
	}
}
