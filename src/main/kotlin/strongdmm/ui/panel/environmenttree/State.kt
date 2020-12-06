package strongdmm.ui.panel.environmenttree

import gnu.trove.map.hash.TLongObjectHashMap
import imgui.type.ImString
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.MapPath
import strongdmm.service.dmi.DmiCache
import strongdmm.ui.panel.environmenttree.model.TreeNode

class State {
    lateinit var providedRecentEnvironmentsWithMaps: Map<String, List<MapPath>>
    lateinit var providedDmiCache: DmiCache

    var isEnvironmentLoading: Boolean = false
    var mapToOpen: MapPath? = null

    var currentEnvironment: Dme? = null
    val treeNodes: TLongObjectHashMap<TreeNode> = TLongObjectHashMap()
    val filteredTreeNodes: MutableList<TreeNode> = mutableListOf()

    var selectedTileItemType: String = ""
    var isSelectedInCycle: Boolean = false
    var isDoOpenSelectedType: Boolean = false

    var isDoCollapseAll: Boolean = false
    val typeFilter: ImString = ImString(50)
}
