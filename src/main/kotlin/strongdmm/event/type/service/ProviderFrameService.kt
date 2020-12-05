package strongdmm.event.type.service

import strongdmm.event.Event
import strongdmm.service.frame.FrameMesh
import strongdmm.service.frame.FramedTile

abstract class ProviderFrameService {
    class ComposedFrame(body: List<FrameMesh>) : Event<List<FrameMesh>, Unit>(body, null)
    class FramedTiles(body: List<FramedTile>) : Event<List<FramedTile>, Unit>(body, null)
}
