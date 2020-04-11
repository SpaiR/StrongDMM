package strongdmm.byond.dmm.parser

class TileContent {
    val content: MutableList<TileObject> = ArrayList(2)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TileContent

        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        return content.hashCode()
    }
}
