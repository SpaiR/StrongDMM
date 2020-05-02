package strongdmm.ui.panel.opened_maps

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.byond.dmm.Dmm

class State {
    lateinit var providedOpenedMaps: Set<Dmm>
    lateinit var providedActionBalanceStorage: TObjectIntHashMap<Dmm>

    var selectedMap: Dmm? = null
}
