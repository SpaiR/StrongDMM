package shortcut

import "github.com/rs/zerolog/log"

var shortcutId uint64

type Shortcuts struct {
	shortcuts []*Shortcut
}

func (s *Shortcuts) Add(shortcut Shortcut) {
	shortcut.id = shortcutId
	log.Print("adding shortcut to shortcuts:", shortcut)
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

func (s *Shortcuts) Dispose() {
	log.Print("disposing shortcuts...")
	for _, shortcut := range s.shortcuts {
		remove(shortcut)
	}
	s.shortcuts = nil
	log.Print("shortcuts disposed")
}
