package strongdmm.event

object EventBus {
    private val subscribersWithArg: MutableMap<Any, MutableList<Any>> = mutableMapOf()
    private val subscribersWithoutArg: MutableMap<Any, MutableList<Any>> = mutableMapOf()

    fun <T, R> register(eventClass: Class<out Event<T, R>>, eventAction: (Event<T, R>) -> Unit) {
        subscribersWithArg.getOrPut(eventClass) { mutableListOf() }.add(eventAction)
    }

    fun <T, R> register(eventClass: Class<out Event<T, R>>, eventAction: () -> Unit) {
        subscribersWithoutArg.getOrPut(eventClass) { mutableListOf() }.add(eventAction)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, R> notify(event: Event<T, R>) {
        subscribersWithArg[event::class.java]?.let { subs ->
            subs.forEach { handler ->
                (handler as (Event<T, R>) -> Unit)(event)
            }
        }
        subscribersWithoutArg[event::class.java]?.let { subs ->
            subs.forEach { handler ->
                (handler as () -> Unit)()
            }
        }
    }
}
