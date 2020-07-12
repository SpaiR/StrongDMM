package strongdmm.util.imgui.markdown

class ImMarkdown {
    val blocks: MutableList<Pair<Block, String>> = mutableListOf()

    enum class Block {
        TEXT, HEADER, LIST
    }
}
