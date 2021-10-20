package shortcut

import "log"

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

func (s *Shortcuts) SetVisible(visible bool) {
	for _, shortcut := range s.shortcuts {
		shortcut.IsVisible = visible
	}
}

func (s *Shortcuts) Dispose() {
	log.Println("[shortcut] disposing shortcuts...")
	for _, shortcut := range s.shortcuts {
		remove(shortcut)
	}
	s.shortcuts = nil
	log.Println("[shortcut] shortcuts disposed")
}
