package strongdmm.event.service

import strongdmm.event.Event
import strongdmm.service.shortcut.Shortcut

abstract class ReactionShortcutService {
    class ShortcutTriggered(body: Shortcut) : Event<Shortcut, Unit>(body, null)
}
