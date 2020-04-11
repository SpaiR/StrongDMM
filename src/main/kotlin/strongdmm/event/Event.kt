package strongdmm.event

/**
 * Events are used to do a communication between application components.
 * By design only "*Ui" and "*Controller" classes could receive them.
 * It's allowed to send events from classes different then ui or controllers, but mechanism should be used with wisdom.
 *
 * Global events are used to show that something globally happened. Like environment was switched or map was closed.
 * Unlike the others, global events could be consumed by any classes.
 *
 * Events like "EnvironmentController" are meant to be consumed ONLY by a specific class.
 * This restriction is checked in runtime.
 *
 * Sometimes it's needed to provide state from one class to another to avoid unneeded events creation.
 * "Event.Provider.*" events should be used for that.
 * Provided variables should be finalized in places they are declared.
 *
 * To make sure that events by themselves are fully self-explanatory, primitive types as well as raw strings should not be used as arguments.
 * Use typealiase instead.
 */
abstract class Event<T, R>(
    val body: T,
    private val callback: ((R) -> Unit)?
) {
    fun reply(response: R) {
        callback?.invoke(response)
    }
}
