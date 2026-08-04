package render

import (
	"sdmm/internal/app/render/bucket/level/chunk/unit"
	"sdmm/internal/dmapi/dmicon"
	"sdmm/internal/util"

	api "github.com/SpaiR/StrongDMM-extension-api"
)

// resolvedAppearance contains only renderer-ready values. It is rebuilt when extension output changes, never while drawing a frame.
type resolvedAppearance struct {
	appearance                     api.AppearancePatch
	compiled                       bool
	visible                        bool
	sprite                         *dmicon.Sprite
	pixelX, pixelY, pixelW, pixelZ int
	r, g, b, a                     float32
	colorSet                       bool
	underlays                      []*dmicon.Sprite
}

func (r *Render) SetAppearancePatches(patches map[uint64]api.AppearancePatch) {
	r.appearances = make(map[uint64]resolvedAppearance, len(patches))
	for id, patch := range patches {
		r.appearances[id] = resolvedAppearance{appearance: patch}
	}
}

func (r *Render) appearanceUnit(u unit.Unit) (unit.Unit, []unit.Unit, bool) {
	patch, ok := r.appearances[u.Instance().Id()]
	if !ok {
		return u, nil, true
	}
	if !patch.compiled {
		patch = compileAppearance(u, patch)
		r.appearances[u.Instance().Id()] = patch
	}
	if !patch.visible {
		return u, nil, false
	}
	main := u
	if patch.sprite != nil {
		main = main.WithSprite(patch.sprite, patch.pixelX, patch.pixelY, 0, 0, patch.pixelW, patch.pixelZ)
	}
	if patch.colorSet {
		main = main.WithColor(patch.r, patch.g, patch.b, patch.a)
	}
	underlays := make([]unit.Unit, 0, len(patch.underlays))
	for _, sprite := range patch.underlays {
		underlays = append(underlays, u.WithSprite(sprite, patch.pixelX, patch.pixelY, 0, 0, patch.pixelW, patch.pixelZ))
	}
	return main, underlays, true
}

func compileAppearance(u unit.Unit, patch resolvedAppearance) resolvedAppearance {
	vars := u.Instance().Prefab().Vars()
	icon, _ := vars.Text("icon")
	state, _ := vars.Text("icon_state")
	dir, _ := vars.Int("dir")
	pixelX, _ := vars.Int("pixel_x")
	pixelY, _ := vars.Int("pixel_y")
	stepX, _ := vars.Int("step_x")
	stepY, _ := vars.Int("step_y")
	pixelW, _ := vars.Int("pixel_w")
	pixelZ, _ := vars.Int("pixel_z")
	appearance := patch.appearance.Appearance
	patch.compiled, patch.visible = true, true
	if appearance.Visible != nil && !*appearance.Visible {
		patch.visible = false
		return patch
	}
	if appearance.Icon != nil {
		icon = *appearance.Icon
	}
	if appearance.IconState != nil {
		state = *appearance.IconState
	}
	if appearance.Dir != nil {
		dir = *appearance.Dir
	}
	if appearance.PixelX != nil {
		pixelX = *appearance.PixelX
	}
	if appearance.PixelY != nil {
		pixelY = *appearance.PixelY
	}
	if appearance.PixelW != nil {
		pixelW = *appearance.PixelW
	}
	if appearance.PixelZ != nil {
		pixelZ = *appearance.PixelZ
	}
	if dmi, err := dmicon.Cache.Get(icon); err == nil {
		if dmiState, found := dmi.States[state]; found {
			patch.sprite = dmiState.SpriteV(dir)
		}
	}
	patch.pixelX, patch.pixelY, patch.pixelW, patch.pixelZ = pixelX+stepX, pixelY+stepY, pixelW, pixelZ
	rc, gc, bc, ac := u.R(), u.G(), u.B(), u.A()
	if appearance.Color != nil {
		rc, gc, bc, _ = util.ParseColor(*appearance.Color).RGBA()
	}
	if appearance.Alpha != nil {
		ac = *appearance.Alpha / 255
	}
	patch.r, patch.g, patch.b, patch.a, patch.colorSet = rc, gc, bc, ac, appearance.Color != nil || appearance.Alpha != nil
	patch.underlays = make([]*dmicon.Sprite, 0, len(patch.appearance.Underlays))
	for _, underlay := range patch.appearance.Underlays {
		underIcon, underState := icon, state
		underDir := dir
		if underlay.Icon != nil {
			underIcon = *underlay.Icon
		}
		if underlay.IconState != nil {
			underState = *underlay.IconState
		}
		if underlay.Dir != nil {
			underDir = *underlay.Dir
		}
		if underlay.Visible != nil && !*underlay.Visible {
			continue
		}
		if sprite, err := dmicon.Cache.GetSpriteV(underIcon, underState, underDir); err == nil {
			patch.underlays = append(patch.underlays, sprite)
		}
	}
	return patch
}
