package dmicon

import (
	"errors"
	"fmt"
	"log"

	"github.com/SpaiR/strongdmm/pkg/dm"
)

var Cache = &IconsCache{icons: make(map[string]*Dmi)}

type IconsCache struct {
	rootDirPath string
	icons       map[string]*Dmi
}

func (i *IconsCache) Free() {
	for _, dmi := range i.icons {
		dmi.free()
	}
	log.Printf("[dmicon] cache free; [%d] icons disposed", len(i.icons))
	i.icons = make(map[string]*Dmi)
}

func (i *IconsCache) SetRootDirPath(rootDirPath string) {
	i.rootDirPath = rootDirPath
	log.Println("[dmicon] cache root dir:", rootDirPath)
}

func (i *IconsCache) Get(icon string) (*Dmi, error) {
	if len(icon) == 0 {
		return nil, errors.New("dmi icon is empty")
	}

	if dmi, ok := i.icons[icon]; ok {
		if dmi == nil {
			return nil, fmt.Errorf("dmi [%s] is nil", icon)
		}
		return dmi, nil
	}

	dmi, err := New(i.rootDirPath + "/" + icon)
	i.icons[icon] = dmi
	return dmi, err
}

func (i *IconsCache) GetState(icon, state string) (*State, error) {
	dmi, err := i.Get(icon)
	if err != nil {
		return nil, err
	}
	return dmi.State(state)
}

func (i *IconsCache) GetSpriteV(icon, state string, dir int) (*Sprite, error) {
	dmiState, err := i.GetState(icon, state)
	if err != nil {
		return nil, err
	}
	return dmiState.SpriteV(dir), nil
}

func (i *IconsCache) GetSprite(icon, state string) (*Sprite, error) {
	return i.GetSpriteV(icon, state, dm.DirDefault)
}

func (i *IconsCache) GetSpriteOrPlaceholder(icon, state string) *Sprite {
	return i.GetSpriteOrPlaceholderV(icon, state, dm.DirDefault)
}

func (i *IconsCache) GetSpriteOrPlaceholderV(icon, state string, dir int) *Sprite {
	if s, err := i.GetSpriteV(icon, state, dir); err == nil {
		return s
	}
	return SpritePlaceholder()
}
