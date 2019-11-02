package strongdmm.event

class Message<T, R>(
    val body: T,
    private val callback: ((R) -> Unit)?
) {
    fun reply(response: R) {
        callback?.invoke(response)
    }
}
