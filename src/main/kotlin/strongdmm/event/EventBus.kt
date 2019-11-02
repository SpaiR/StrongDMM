package strongdmm.event

import gnu.trove.map.hash.TShortObjectHashMap
import strongdmm.util.extension.getOrPut

object EventBus {
    private val subscribers: TShortObjectHashMap<MutableList<Any>> = TShortObjectHashMap()

    fun <T, R> register(event: Event, eventAction: (Message<T, R>) -> Unit) {
        subscribers.getOrPut(event.ordinal.toShort()) { mutableListOf() }.add(eventAction)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T, R> notify(event: Event, msg: Message<T, R>) {
        subscribers[event.ordinal.toShort()]?.let { subs ->
            subs.forEach { handler ->
                (handler as (Message<T, R>) -> Unit)(msg)
            }
        }
    }
}
