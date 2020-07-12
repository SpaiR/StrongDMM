package strongdmm.util.imgui.markdown

import imgui.ImGui
import strongdmm.window.Window

object ImGuiMarkdown {
    private const val HEADER_PREFIX = "# "
    private const val LIST_PREFIX = " * "

    fun parse(text: String): ImMarkdown {
        val markdown = ImMarkdown()

        text.lineSequence().forEach { line ->
            when {
                line.startsWith(HEADER_PREFIX) -> {
                    markdown.blocks.add(ImMarkdown.Block.HEADER to line.substringAfter(HEADER_PREFIX))
                }
                line.startsWith(LIST_PREFIX) -> {
                    markdown.blocks.add(ImMarkdown.Block.LIST to line.substringAfter(LIST_PREFIX))
                }
                else -> {
                    markdown.blocks.add(ImMarkdown.Block.TEXT to line)
                }
            }
        }

        return markdown
    }

    fun render(markdown: ImMarkdown) {
        markdown.blocks.forEach { (block, text) ->
            when (block) {
                ImMarkdown.Block.TEXT -> renderText(text)
                ImMarkdown.Block.HEADER -> renderHeader(text)
                ImMarkdown.Block.LIST -> renderList(text)
            }
        }
    }

    fun renderText(text: String) {
        ImGui.textWrapped(text)
    }

    fun renderHeader(text: String) {
        ImGui.pushFont(Window.headerFont)
        ImGui.textWrapped(text)
        ImGui.popFont()
        ImGui.separator()
    }

    fun renderList(text: String) {
        ImGui.bullet()
        ImGui.sameLine()
        ImGui.textWrapped(text)
    }
}
