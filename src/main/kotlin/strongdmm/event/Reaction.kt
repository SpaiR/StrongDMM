package strongdmm.event

abstract class Reaction {
    class ApplicationBlockChanged(applicationBlockStatus: Boolean) : Event<Boolean, Unit>(applicationBlockStatus, null)
}
