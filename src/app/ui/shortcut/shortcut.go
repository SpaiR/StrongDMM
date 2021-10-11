package shortcut

import (
	"fmt"
	"log"
	"sort"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
)

type Shortcut struct {
	Id           string
	FirstKey     glfw.Key
	FirstKeyAlt  glfw.Key
	SecondKey    glfw.Key
	SecondKeyAlt glfw.Key
	ThirdKey     glfw.Key
	ThirdKeyAlt  glfw.Key
	Action       func()
	IsEnabled    func() bool
}

func (s Shortcut) String() string {
	return fmt.Sprintf(
		"Id: %s, "+
			"FirstKey: %d, FirstKeyAlt: %d, "+
			"SecondKey: %d, SecondKeyAlt: %d, "+
			"ThirdKey: %d, ThirdKeyAlt: %d, "+
			"HasAction: %t, HasIsEnabled: %t, "+
			"Wheight: %d",
		s.Id,
		s.FirstKey, s.FirstKeyAlt,
		s.SecondKey, s.SecondKeyAlt,
		s.ThirdKey, s.ThirdKeyAlt,
		s.Action != nil, s.IsEnabled != nil,
		s.weight(),
	)
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
	if s.SecondKey == 0 && s.ThirdKey == 0 {
		if imgui.IsKeyPressed(int(s.FirstKey)) || imgui.IsKeyPressed(int(s.FirstKeyAlt)) {
			return true
		}
	}
	if !imgui.IsKeyDown(int(s.FirstKey)) && !imgui.IsKeyDown(int(s.FirstKeyAlt)) {
		return false
	}

	if s.SecondKey != 0 && s.ThirdKey == 0 {
		if imgui.IsKeyPressed(int(s.SecondKey)) || imgui.IsKeyPressed(int(s.SecondKeyAlt)) {
			return true
		}
	}
	if !imgui.IsKeyDown(int(s.SecondKey)) && !imgui.IsKeyDown(int(s.SecondKeyAlt)) {
		return false
	}

	return s.ThirdKey != 0 && (imgui.IsKeyPressed(int(s.ThirdKey)) || imgui.IsKeyPressed(int(s.ThirdKeyAlt)))
}

var shortcuts []Shortcut

func Add(shortcut Shortcut) {
	log.Println("[shortcut] added:", shortcut)
	shortcuts = append(shortcuts, shortcut)
}

func Remove(shortcut Shortcut) {
	log.Println("[shortcut] removed:", shortcut)
	for idx, s := range shortcuts {
		if s.Id == shortcut.Id {
			shortcuts = append(shortcuts[:idx], shortcuts[idx+1:]...)
			break
		}
	}
}

func Process() {
	var pressedShortcuts []Shortcut

	for _, shortcut := range shortcuts {
		if shortcut.isPressed() {
			pressedShortcuts = append(pressedShortcuts, shortcut)
		}
	}

	if len(pressedShortcuts) != 0 {
		sort.Slice(pressedShortcuts, func(i, j int) bool {
			return pressedShortcuts[i].weight() > pressedShortcuts[j].weight()
		})

		if shortcut := pressedShortcuts[0]; shortcut.IsEnabled == nil || shortcut.IsEnabled() {
			log.Println("[shortcut] triggered:", shortcut)
			shortcut.Action()
		}
	}
}
