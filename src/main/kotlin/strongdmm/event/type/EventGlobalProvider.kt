package strongdmm.event.type

import gnu.trove.map.hash.TObjectIntHashMap
import imgui.ImBool
import strongdmm.byond.dmm.Dmm
import strongdmm.controller.frame.FrameMesh
import strongdmm.controller.frame.FramedTile
import strongdmm.event.AbsoluteFilePath
import strongdmm.event.Event
import strongdmm.event.VisibleFilePath

abstract class EventGlobalProvider {
    class InstanceLocatorPanelUiOpen(body: ImBool) : Event<ImBool, Unit>(body, null)
    class MapHolderControllerOpenedMaps(body: Set<Dmm>) : Event<Set<Dmm>, Unit>(body, null)
    class MapHolderControllerAvailableMaps(body: Set<Pair<AbsoluteFilePath, VisibleFilePath>>) :
        Event<Set<Pair<AbsoluteFilePath, VisibleFilePath>>, Unit>(body, null)
    class FrameControllerComposedFrame(body: List<FrameMesh>) : Event<List<FrameMesh>, Unit>(body, null)
    class FrameControllerFramedTiles(body: List<FramedTile>) : Event<List<FramedTile>, Unit>(body, null)
    class ActionControllerActionBalanceStorage(body: TObjectIntHashMap<Dmm>) : Event<TObjectIntHashMap<Dmm>, Unit>(body, null)
    class CanvasControllerFrameAreas(body: ImBool) : Event<ImBool, Unit>(body, null)
}
