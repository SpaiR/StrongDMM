package strongdmm.controller.preferences

enum class MapSaveMode : Selectable {
    PROVIDED,
    BYOND,
    TGM;

    override fun toString(): String {
        return name.toLowerCase().capitalize()
    }
}
