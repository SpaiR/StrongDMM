package shortcut

import "log"

type Shortcuts struct {
	shortcuts []Shortcut
}

func (s *Shortcuts) Add(shortcut Shortcut) {
	log.Println("[shortcut] adding shortcut to shortcuts:", shortcut)
	s.shortcuts = append(s.shortcuts, shortcut)
	Add(shortcut)
}

func (s *Shortcuts) Dispose() {
	log.Println("[shortcut] disposing shortcuts...")
	for _, shortcut := range s.shortcuts {
		Remove(shortcut)
	}
	s.shortcuts = nil
	log.Println("[shortcut] shortcuts disposed")
}
