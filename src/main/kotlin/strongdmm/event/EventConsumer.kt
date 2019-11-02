package strongdmm.event

interface EventConsumer {
    fun <T, R> consumeEvent(event: Event, eventAction: (Message<T, R>) -> Unit) {
        EventBus.register(event, eventAction)
    }
}
