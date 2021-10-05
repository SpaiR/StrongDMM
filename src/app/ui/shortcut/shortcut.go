package shortcut

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
)

type Shortcut struct {
	Name      string
	FirstKey  glfw.Key
	SecondKey glfw.Key
	ThirdKey  glfw.Key
	Action    func()
	IsEnabled func() bool
}

func (s Shortcut) String() string {
	return fmt.Sprintf(
		"Name: %s, FirstKey: %d, SecondKey: %d, ThirdKey: %d, HasAction: %t, HasIsEnabled: %t",
		s.Name, s.FirstKey, s.SecondKey, s.ThirdKey, s.Action != nil, s.IsEnabled != nil,
	)
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

func (s Shortcut) isPressed() bool {
	if s.SecondKey == 0 && s.ThirdKey == 0 && imgui.IsKeyPressed(int(s.FirstKey)) {
		return true
	}
	if !imgui.IsKeyDown(int(s.FirstKey)) {
		return false
	}

	if s.SecondKey != 0 && s.ThirdKey == 0 && imgui.IsKeyPressed(int(s.SecondKey)) {
		return true
	}
	if !imgui.IsKeyDown(int(s.SecondKey)) {
		return false
	}

	return s.ThirdKey != 0 && imgui.IsKeyPressed(int(s.ThirdKey))
}

var shortcuts []Shortcut

func Add(shortcut Shortcut) {
	log.Println("[shortcut] added:", shortcut)
	shortcuts = append(shortcuts, shortcut)
}

func Process() {
	var shortcutToTrigger Shortcut

	for _, shortcut := range shortcuts {
		if !shortcut.isPressed() {
			continue
		}

		if shortcut.IsEnabled != nil && !shortcut.IsEnabled() {
			continue
		}

		if shortcutToTrigger.isNil() || shortcutToTrigger.weight() < shortcut.weight() {
			shortcutToTrigger = shortcut
		}
	}

	if !shortcutToTrigger.isNil() {
		log.Println("[shortcut] triggered:", shortcutToTrigger)
		shortcutToTrigger.Action()
	}
}
