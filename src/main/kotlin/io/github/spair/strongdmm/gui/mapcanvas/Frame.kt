package io.github.spair.strongdmm.gui.mapcanvas

// Class to control the moment when we need to update OpenGL canvas.
// Thus while we doesn't touch map canvas we won't spend any CPU to process stuff.
object Frame {

    private const val NO_FRAMES_TO_UPD = 0
    private const val DEFAULT_FRAMES_TO_UPD = 2 // To render everything properly we need 2 frames

    private var updateCounter = DEFAULT_FRAMES_TO_UPD

    fun update() {
        updateCounter = DEFAULT_FRAMES_TO_UPD
    }

    fun hasUpdates() = updateCounter-- >= NO_FRAMES_TO_UPD
}
