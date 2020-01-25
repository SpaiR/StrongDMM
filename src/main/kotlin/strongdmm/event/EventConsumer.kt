package strongdmm.event

import imgui.ImBool

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
        require(event.name.contains("\$Global$") || event.name.contains("\$${this::class.java.name.substringAfterLast('.')}$")) {
            "Unable to consume the event: ${event.name}! Event should be global or designed for a specific class."
        }

        if (event.name.contains("\$Provider$")) {
            val eventBodyClass = event.constructors[0].parameterTypes[0].name
            require(eventBodyClass == ImBool::class.java.name) {
                "Provider class is able to have only an ImBool class as a body."
            }
        }
    }
}
