package strongdmm.ui.panel.environment_tree

import gnu.trove.map.hash.TLongObjectHashMap
import imgui.ImString
import strongdmm.byond.dme.Dme
import strongdmm.ui.panel.environment_tree.model.TreeNode

class State {
    lateinit var providedRecentEnvironments: List<String>

    var isEnvironmentLoading: Boolean = false

    var currentEnvironment: Dme? = null
    val treeNodes: TLongObjectHashMap<TreeNode> = TLongObjectHashMap()

    var selectedTileItemType: String = ""
    var isSelectedInCycle: Boolean = false
    var isDoOpenSelectedType: Boolean = false

    var isDoCollapseAll: Boolean = false
    val typeFilter: ImString = ImString(50)

    var createdTeeNodesInCycle: Int = 0
}
