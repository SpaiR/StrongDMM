package strongdmm.ui.environment_tree_panel

import gnu.trove.map.hash.TLongObjectHashMap
import imgui.ImString
import strongdmm.byond.dme.Dme
import strongdmm.ui.environment.tree.panel.model.TreeNode

class State {
    lateinit var providedRecentEnvironments: List<String>

    var isEnvironmentLoading: Boolean = false

    var currentEnvironment: Dme? = null
    val treeNodes: TLongObjectHashMap<TreeNode> = TLongObjectHashMap()

    var activeTileItemType: String = ""
    var isSelectedInCycle: Boolean = false

    var isDoCollapseAll: Boolean = false
    val typeFilter: ImString = ImString(50)

    var createdTeeNodesInCycle: Int = 0
}
