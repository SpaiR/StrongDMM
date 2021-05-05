package shortcut

import (
	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
)

type Shortcut struct {
	FirstKey  glfw.Key
	SecondKey glfw.Key
	ThirdKey  glfw.Key
	Action    func()
}

func (s Shortcut) isNil() bool {
	return s.Action == nil
}

func (s Shortcut) weight() int {
	weight := 0
	if s.SecondKey != 0 {
		weight++
	}
	if s.ThirdKey != 0 {
		weight++
	}
	return weight
}

var shortcuts []Shortcut

func Add(shortcut Shortcut) {
	shortcuts = append(shortcuts, shortcut)
}

func Process() {
	var shortcutToTrigger Shortcut

	for _, s := range shortcuts {
		if s.SecondKey == 0 && s.ThirdKey == 0 && imgui.IsKeyPressed(int(s.FirstKey)) {
			if shortcutToTrigger.isNil() || shortcutToTrigger.weight() < s.weight() {
				shortcutToTrigger = s
			}
		} else if imgui.IsKeyDown(int(s.FirstKey)) {
			if s.ThirdKey != 0 {
				if imgui.IsKeyDown(int(s.SecondKey)) && imgui.IsKeyPressed(int(s.ThirdKey)) {
					shortcutToTrigger = s
					break
				}
			} else if s.SecondKey != 0 {
				if imgui.IsKeyPressed(int(s.SecondKey)) {
					if shortcutToTrigger.isNil() || shortcutToTrigger.weight() < s.weight() {
						shortcutToTrigger = s
					}
				}
			}
		}
	}

	if !shortcutToTrigger.isNil() {
		shortcutToTrigger.Action()
	}
}
