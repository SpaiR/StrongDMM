package strongdmm.event

interface EventSender {
    fun <T, R> sendEvent(event: Event, content: T, reply: (R) -> Unit) {
        EventBus.notify(event, Message(content, reply))
    }

    fun <T> sendEvent(event: Event, content: T) {
        EventBus.notify<T, Unit>(event, Message(content, null))
    }

    fun sendEvent(event: Event) {
        sendEvent(event, Unit)
    }

    fun <R> sendEvent(event: Event, reply: (R) -> Unit) {
        sendEvent(event, Unit, reply)
    }
}
