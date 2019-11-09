package strongdmm.event

interface EventSender {
    fun <T, R> sendEvent(event: Event<T, R>) {
        EventBus.notify(event)
    }
}
