package strongdmm.controller.preferences

enum class MapSaveMode(
    val desc: String
) {
    PROVIDED("The format that the map is currently using will be used for saving."),
    BYOND("The map will always be saved in BYOND format."),
    TGM("The map will always be saved in TGM format.")
}
