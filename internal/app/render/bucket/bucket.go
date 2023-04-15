package bucket

import (
	"sort"

	"sdmm/internal/app/render/bucket/level"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/util"

	"github.com/rs/zerolog/log"
)

// Bucket contains data needed to render the map.
// The Bucket itself is made of Level's which are made of Chunk's.
type Bucket struct {
	Levels []int
	levels map[int]*level.Level
}

func New() *Bucket {
	return &Bucket{
		levels: make(map[int]*level.Level),
	}
}

// UpdateLevel updates a specific level of the bucket. If the level not exist, will create it at first.
func (b *Bucket) UpdateLevel(dmm *dmmap.Dmm, levelValue int, tilesToUpdate []util.Point) {
	log.Printf("updating bucket with [%s]...", dmm.Path.Readable)
	b.getOrCreateLevel(dmm, levelValue).Update(dmm, tilesToUpdate)
	log.Print("bucket updated")
}

// Level returns a specific level of the bucket or nil if it's not exist.
func (b *Bucket) Level(level int) *level.Level {
	return b.levels[level]
}

func (b *Bucket) getOrCreateLevel(dmm *dmmap.Dmm, levelValue int) *level.Level {
	if l, ok := b.levels[levelValue]; ok {
		return l
	} else {
		log.Print("created level:", levelValue)
		l = level.New(dmm, levelValue)
		b.Levels = append(b.Levels, levelValue)
		sort.Ints(b.Levels)
		b.levels[levelValue] = l
		return l
	}
}
