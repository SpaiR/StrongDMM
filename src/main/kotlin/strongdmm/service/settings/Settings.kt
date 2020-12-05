package strongdmm.service.settings

import strongdmm.service.changelog.ChangelogServiceSettings
import strongdmm.service.pinnedvariables.PinnedVariablesServiceSettings
import strongdmm.ui.panel.screenshot.model.ScreenshotPanelUiSettings

class Settings {
    var pinnedVariablesServiceSettings = PinnedVariablesServiceSettings()
    var screenshotPanelUiSettings = ScreenshotPanelUiSettings()
    var changelogServiceSettings = ChangelogServiceSettings()
}
