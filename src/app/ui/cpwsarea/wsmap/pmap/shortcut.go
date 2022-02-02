package pmap

import (
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/cpwsarea/wsmap/tools"
	"sdmm/app/ui/shortcut"
)

func (p *PaneMap) addShortcuts() {
	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#selectAddTool",
		FirstKey:    glfw.Key1,
		FirstKeyAlt: glfw.KeyKP1,
		Action:      selectAddTool,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#selectFillTool",
		FirstKey:    glfw.Key2,
		FirstKeyAlt: glfw.KeyKP2,
		Action:      selectFillTool,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#selectSelectTool",
		FirstKey:    glfw.Key3,
		FirstKeyAlt: glfw.KeyKP3,
		Action:      selectSelectTool,
	})

	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#doDeselectAll",
		FirstKey:    shortcut.KeyModLeft(),
		FirstKeyAlt: shortcut.KeyModRight(),
		SecondKey:   glfw.KeyD,
		Action:      p.DoDeselect,
	})

	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleArea",
		FirstKey:     shortcut.KeyModLeft(),
		FirstKeyAlt:  shortcut.KeyModRight(),
		SecondKey:    glfw.Key1,
		SecondKeyAlt: glfw.KeyKP1,
		Action:       p.doToggleArea,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleTurf",
		FirstKey:     shortcut.KeyModLeft(),
		FirstKeyAlt:  shortcut.KeyModRight(),
		SecondKey:    glfw.Key2,
		SecondKeyAlt: glfw.KeyKP2,
		Action:       p.doToggleTurf,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleObject",
		FirstKey:     shortcut.KeyModLeft(),
		FirstKeyAlt:  shortcut.KeyModRight(),
		SecondKey:    glfw.Key3,
		SecondKeyAlt: glfw.KeyKP3,
		Action:       p.doToggleObject,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:         "pmap#doToggleMob",
		FirstKey:     shortcut.KeyModLeft(),
		FirstKeyAlt:  shortcut.KeyModRight(),
		SecondKey:    glfw.Key4,
		SecondKeyAlt: glfw.KeyKP4,
		Action:       p.doToggleMob,
	})

	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#doPreviousLevel",
		FirstKey:    shortcut.KeyModLeft(),
		FirstKeyAlt: shortcut.KeyModRight(),
		SecondKey:   glfw.KeyDown,
		Action:      p.doPreviousLevel,
		IsEnabled:   p.hasPreviousLevel,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:        "pmap#doNextLevel",
		FirstKey:    shortcut.KeyModLeft(),
		FirstKeyAlt: shortcut.KeyModRight(),
		SecondKey:   glfw.KeyUp,
		Action:      p.doNextLevel,
		IsEnabled:   p.hasNextLevel,
	})

	p.shortcuts.Add(shortcut.Shortcut{
		Name:     "pmap#doMoveCameraUp",
		FirstKey: glfw.KeyUp,
		Action:   p.doMoveCameraUp,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:     "pmap#doMoveCameraDown",
		FirstKey: glfw.KeyDown,
		Action:   p.doMoveCameraDown,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:     "pmap#doMoveCameraLeft",
		FirstKey: glfw.KeyLeft,
		Action:   p.doMoveCameraLeft,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:     "pmap#doMoveCameraRight",
		FirstKey: glfw.KeyRight,
		Action:   p.doMoveCameraRight,
	})

	p.shortcuts.Add(shortcut.Shortcut{
		Name:     "pmap#doZoomIn",
		FirstKey: glfw.KeyEqual,
		Action:   p.doZoomIn,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:     "pmap#doZoomIn",
		FirstKey: glfw.KeyKPEqual,
		Action:   p.doZoomIn,
	})
	p.shortcuts.Add(shortcut.Shortcut{
		Name:     "pmap#doZoomOut",
		FirstKey: glfw.KeyMinus,
		Action:   p.doZoomOut,
	})
}

func (p *PaneMap) doToggleArea() {
	p.app.PathsFilter().TogglePath("/area")
}

func (p *PaneMap) doToggleTurf() {
	p.app.PathsFilter().TogglePath("/turf")
}

func (p *PaneMap) doToggleObject() {
	p.app.PathsFilter().TogglePath("/obj")
}

func (p *PaneMap) doToggleMob() {
	p.app.PathsFilter().TogglePath("/mob")
}

func (p *PaneMap) DoDeselect() {
	tools.Tools()[tools.TNGrab].OnDeselect()
}

func (p *PaneMap) doMoveCameraUp() {
	p.translateCanvas(0, p.calcManualCanvasTranslateShift())
}

func (p *PaneMap) doMoveCameraDown() {
	p.translateCanvas(0, -p.calcManualCanvasTranslateShift())
}

func (p *PaneMap) doMoveCameraLeft() {
	p.translateCanvas(p.calcManualCanvasTranslateShift(), 0)
}

func (p *PaneMap) doMoveCameraRight() {
	p.translateCanvas(-p.calcManualCanvasTranslateShift(), 0)
}

func (p *PaneMap) doZoomIn() {
	camera := p.canvas.Render().Camera()

	scale := camera.Scale * -scaleFactor

	offsetX := (p.size.X - p.size.X/2) / scale / 2
	offsetY := (p.size.Y - p.size.Y/2) / scale / 2

	camera.Translate(offsetX, offsetY)
	camera.Zoom(true, scaleFactor)
}

func (p *PaneMap) doZoomOut() {
	camera := p.canvas.Render().Camera()

	offsetX := (p.size.X - p.size.X/2) / camera.Scale / 2
	offsetY := (p.size.Y - p.size.Y/2) / camera.Scale / 2

	camera.Translate(offsetX, offsetY)
	camera.Zoom(false, scaleFactor)
}
