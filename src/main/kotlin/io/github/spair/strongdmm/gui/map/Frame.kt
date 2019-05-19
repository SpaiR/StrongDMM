package io.github.spair.strongdmm.gui.map

// Class to control frame update stuff.
// Every time ::update method called, OpenGL will update map canvas during next two frames.
// This approach helps to reduce CPU/GPU usage when nothing happens on the screen.
//
// While rendering VisualComposer tries to cache render instances information, so sometimes it's needed
// to enforce update through that cache. To do that ::update with 'true' argument should be called.
object Frame {

    private const val NO_FRAMES_TO_UPD = 0
    private const val DEFAULT_FRAMES_TO_UPD = 2 // To render everything properly we need 2 frames

    private var updateCounter = DEFAULT_FRAMES_TO_UPD
    private var isForced = false  // Will enforce VisualComposer to provide new render instances

    fun update() {
        updateCounter = DEFAULT_FRAMES_TO_UPD
    }

    fun update(isForced: Boolean) {
        if (isForced) this.isForced = isForced
        updateCounter = DEFAULT_FRAMES_TO_UPD
    }

    fun hasUpdates() = updateCounter-- >= NO_FRAMES_TO_UPD

    fun isForced() = if (isForced) {
        isForced = false
        true
    } else {
        false
    }
}
