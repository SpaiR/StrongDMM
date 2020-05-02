package strongdmm.event

interface EventHandler {
    fun <T, R> consumeEvent(event: Class<out Event<T, R>>, eventAction: (Event<T, R>) -> Unit) {
        EventBus.register(event, eventAction)
    }

    fun <T, R> consumeEvent(event: Class<out Event<T, R>>, eventAction: () -> Unit) {
        EventBus.register(event, eventAction)
    }

    fun <T, R> sendEvent(event: Event<T, R>) {
        EventBus.notify(event)
    }
}
