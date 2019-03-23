package io.github.spair.strongdmm.gui.mapcanvas

// Class to control frame stuff.
// Thus while we doesn't touch map, we won't spend any CPU to process things.
object Frame {

    private const val NO_FRAMES_TO_UPD = 0
    private const val DEFAULT_FRAMES_TO_UPD = 2 // To render everything properly we need 2 frames

    private var updateCounter = DEFAULT_FRAMES_TO_UPD
    private var isForced = false  // Will enforce VisualComposer to provide new render instances

    fun update(isForced: Boolean = false) {
        this.isForced = isForced
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
