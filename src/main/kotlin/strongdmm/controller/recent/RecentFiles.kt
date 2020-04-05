package strongdmm.controller.recent

import strongdmm.byond.dmm.MapPath

class RecentFiles {
    val environments: MutableList<String> = mutableListOf()
    val maps: MutableMap<String, MutableList<MapPath>> = mutableMapOf()
}
