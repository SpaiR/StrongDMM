package strongdmm.event.type.service

import strongdmm.event.Event
import strongdmm.util.imgui.markdown.ImMarkdown

abstract class ProviderChangelogService {
    class ChangelogMarkdown(body: ImMarkdown) : Event<ImMarkdown, Unit>(body, null)
}
