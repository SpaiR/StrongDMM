package strongdmm.event

interface EventConsumer {
    fun <T, R> consumeEvent(event: Class<out Event<T, R>>, eventAction: (Event<T, R>) -> Unit) {
        checkConsumerClass(event)
        EventBus.register(event, eventAction)
    }

    fun <T, R> consumeEvent(event: Class<out Event<T, R>>, eventAction: () -> Unit) {
        checkConsumerClass(event)
        EventBus.register(event, eventAction)
    }

    private fun <T, R> checkConsumerClass(event: Class<out Event<T, R>>) {
        require(
            event.name.contains("Reaction") ||
                event.name.contains("Provider") ||
                event.name.contains("${this::class.java.name.substringAfterLast('.')}$")
        ) {
            "Unable to consume the event: ${event.name}! Event should be global or designed for a specific class."
        }
    }
}
