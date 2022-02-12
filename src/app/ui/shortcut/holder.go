package shortcut

import (
	"github.com/SpaiR/imgui-go"
	"log"
)

var shortcutId uint64

type Shortcuts struct {
	shortcuts []*Shortcut
}

func (s *Shortcuts) Add(shortcut Shortcut) {
	shortcut.id = shortcutId
	log.Println("[shortcut] adding shortcut to shortcuts:", shortcut)
	pShortcut := &shortcut
	s.shortcuts = append(s.shortcuts, pShortcut)
	add(pShortcut)
	shortcutId++
}

func (s *Shortcuts) Visible() bool {
	for _, shortcut := range s.shortcuts {
		if !shortcut.IsVisible {
			return false
		}
	}
	return true
}

func (s *Shortcuts) SetVisible(visible bool) {
	for _, shortcut := range s.shortcuts {
		shortcut.IsVisible = visible
	}
}

func (s *Shortcuts) SetVisibleIfFocused() {
	s.SetVisible(imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows))
}

func (s *Shortcuts) Dispose() {
	log.Println("[shortcut] disposing shortcuts...")
	for _, shortcut := range s.shortcuts {
		remove(shortcut)
	}
	s.shortcuts = nil
	log.Println("[shortcut] shortcuts disposed")
}
