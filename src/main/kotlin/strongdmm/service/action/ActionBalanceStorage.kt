package strongdmm.service.action

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.byond.dmm.Dmm

class ActionBalanceStorage(
    private val storage: TObjectIntHashMap<Dmm>
) {
    fun isMapModified(map: Dmm): Boolean = storage.containsKey(map) && storage[map] != 0
}
