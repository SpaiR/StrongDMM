package strongdmm.event

interface EventConsumer {
    fun <T, R> consumeEvent(event: Class<out Event<T, R>>, eventAction: (Event<T, R>) -> Unit) {
        EventBus.register(event, eventAction)
    }

    fun <T, R> consumeEvent(event: Class<out Event<T, R>>, eventAction: () -> Unit) {
        EventBus.register(event, eventAction)
    }
}
