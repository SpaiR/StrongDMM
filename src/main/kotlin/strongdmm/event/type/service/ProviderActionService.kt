package strongdmm.event.type.service

import strongdmm.event.Event
import strongdmm.service.action.ActionBalanceStorage as ABS

abstract class ProviderActionService {
    class ActionBalanceStorage(body: ABS) : Event<ABS, Unit>(body, null)
}
