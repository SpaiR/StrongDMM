package strongdmm.event.type.service

import strongdmm.event.Event

abstract class ReactionScreenshotService {
    class ScreenshotTakeStarted private constructor() : Event<Unit, Unit>(Unit, null) {
        companion object {
            val SIGNAL = ScreenshotTakeStarted()
        }
    }

    class ScreenshotTakeStopped private constructor() : Event<Unit, Unit>(Unit, null) {
        companion object {
            val SIGNAL = ScreenshotTakeStopped()
        }
    }
}
