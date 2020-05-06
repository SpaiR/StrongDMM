package strongdmm.service.settings

import strongdmm.service.changelog.ChangelogServiceSettings
import strongdmm.service.pinned_variables.PinnedVariablesServiceSettings
import strongdmm.ui.panel.screenshot.model.ScreenshotPanelUiSettings

class Settings {
    var pinnedVariablesServiceSettings = PinnedVariablesServiceSettings()
    var screenshotPanelUiSettings = ScreenshotPanelUiSettings()
    var changelogServiceSettings = ChangelogServiceSettings()
}
