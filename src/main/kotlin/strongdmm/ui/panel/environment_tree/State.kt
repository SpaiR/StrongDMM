package strongdmm.ui.panel.environment_tree

import gnu.trove.map.hash.TLongObjectHashMap
import imgui.type.ImString
import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.MapPath
import strongdmm.ui.panel.environment_tree.model.TreeNode

class State {
    lateinit var providedRecentEnvironmentsWithMaps: Map<String, List<MapPath>>

    var isEnvironmentLoading: Boolean = false
    var mapToOpen: MapPath? = null

    var currentEnvironment: Dme? = null
    val treeNodes: TLongObjectHashMap<TreeNode> = TLongObjectHashMap()

    var selectedTileItemType: String = ""
    var isSelectedInCycle: Boolean = false
    var isDoOpenSelectedType: Boolean = false

    var isDoCollapseAll: Boolean = false
    val typeFilter: ImString = ImString(50)

    var createdTeeNodesInCycle: Int = 0
}
