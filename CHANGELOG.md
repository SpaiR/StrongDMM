# v2.15.0.alpha

Added support for modifying the "pixel_z" and "pixel_w" variables. @Drulikar

# v2.14.2.alpha

Updated the Fill Tool's border mode to prevent double placement on corners. @mc-oofert

# v2.14.1.alpha

Fixed an issue where the editor would not open projects with minor warnings.

# v2.14.0.alpha

* Bumped SpaceManiac/SpacemanDMM BYOND parser to the latest.
* The editor now properly handles parsing issues detected by SpacemanDMM and will no longer crash silently. If a compilation-breaking issue is found during parsing, the editor will display a dialog box indicating the error location.

# v2.13.0.alpha

Tree nodes will respect the atom's dir var when rendering a preview. @Drulikar

# v2.12.0.alpha

* Holding Shift while dragging with the Move Tool now drags offsets instead. @mc-oofert
* Removed effect layers to prevent issues with existing multipliers and layering defines. @LemonInTheDark
* Updated the version of Rust and SpacemanDMM library used to latest.

# v2.11.0.alpha

* Fixed the overly wide missed icon placeholder. @out-of-phaze
* Added an option to save screenshots to the clipboard, enabled by default. @mc-oofert

# v2.10.0.alpha

* Bumped SpaceManiac/SpacemanDMM BYOND parser.
* Added "Copy Type" action for the environment tree context menu (right-click on the item). @mc-oofert

# v2.9.0.alpha

* Fixed map rendering for AMD GPUs.
* Added "Go to Coords" functionality. Use "Ctrl+G" ("Cmd+G" for macOS) or navigate to "Edit->Go to Coords". @mc-oofert

# v2.8.1.alpha

Fixed a crash in the recent "Move Tool".

# v2.8.0.alpha

* Added border-fill mode for the Fill Tool, accessible by holding CTRL. @mc-oofert
* "Find On Map" now starts a search by path. @mc-oofert
* Added the Move Tool, allowing movement of a singular object. @mc-oofert
* Introduced a basic autocorrect function for variable editing, automatically enclosing strings and lists. @mc-oofert
* Added a "Screenshot in Selection" checkbox to the Screenshot utility, enabling screenshots of only the selected area by the Grab Tool. @mc-oofert

# v2.7.4.alpha

Fixed whitespace duplication while parsing lists. @LemonInTheDark

# v2.7.3.alpha

Fixed list whitespace parsing issues. @itsmeow

# v2.7.2.alpha

* Fixed infinite scrolling when exiting the canvas during a scroll. @itsmeow
* Fixed deletion processing for out-of-bounds tiles. @itsmeow

# v2.7.1.alpha

Fixed respect for Z-level when searching for an object with "Find on Map".

# v2.7.0.alpha

* Renamed save format from "DM" to "DMM" for more accurate terminology.
* Improved environment parsing: linking between parent types and children now uses a specific parent_type variable. @SpaceManiac
* Implemented support for editing "tmp, const, and static" variables, similar to DreamMaker. @SpaceManiac

# v2.6.2.alpha

Fixed an issue where the area bounding size for the Grab tool was off by 1.

# v2.6.1.alpha

* Reverted: renamed "New Map" button to "Create Map" and moved it to the "Edit" menu.
* When selecting multiple maps in the available maps pane, the open button tooltip will show only a part of those maps.

# v2.6.0.alpha

### New open flow!

Since ancient times DreamMaker was teaching us that to open a map we need to open an environment first.<br>
But things have changed...<br>
Now there is no separate button to open either environment or a map. The "Open" button does all of that.<br>
If there is no environment loaded, StrongDMM will try to find and open one. The search is recursive, bottom-up through the directories.<br>

Referencing to the new open flow, now you can pass any .dme/.dmm files to StrongDMM as startup arguments.<br>
If you have passed only .dmm files, it will do the same as it is said above: automatically find and open .dme file for it.

### Improved access to recent environments and maps

The whole startup workspace was redesigned to provide more useful information.<br>
Now, by default, it contains recent environments and maps. But when you have opened environment it will show you all available maps for it.

### Available maps

StrongDMM v1 had a one called feature. And now it is fully back-ported to the v2.<br>
The new version also is more robust than the previous one. It supports hotkeys to select a range of maps to open.<br>
There were a bunch of requests to add an ability to open multiple maps at once, so now it is available.

### Search with replace/delete

Search functionality was missing a replace/delete feature.<br>
Back-ported functionality is able to replace not only with prefabs by passing their pass, but with any selected instance.

### Search filter

Also, search filter was totally revamped. Now it is more intuitive to use and easier to understand.<br>
Toggle a specific "Filter" button or use a hotkey when the search panel is focused: "F" key.

### Other Improvements and Fixes

* Renamed "Options" menu to "View".
* Renamed "New Map" button to "Create Map" and moved it to the "Edit" menu.
* Removed "Recent Environments" menu button.
* Added "Close Environment" menu button.
* Environment tree now respects prefab "color" variable.
* Slightly improved "Grab" tool performance.
* Enhanced map parsing algorithm (now shows lines with an error). @d0sboots
* Added a preference option for enabling/disabling auto-updates (enabled by default).
* Added status indicators for the "Grab" tool: size and area bounds coordinates.
* Added an information dialog when loading an environment.
* Fixed invalid shortcuts handling with a custom layout.

# v2.5.4.alpha

Fixed crashes when modifying maps with specific sizes. The issue was connected with internal chunks generation.<br>
For maps with axis sizes less than a chunk size they were generated improperly.

# v2.5.3.alpha

### Fixes

* Fixed rare crashes when deleting instances.
* Updated build dependencies. [maintenance]

# v2.5.2.alpha

Fixed issues with "option" preference options. @KIBORG04

# v2.5.1.alpha

Fixed the issue where the keypad "enter" key did not work. @igorsaux

# v2.5.0.alpha

### Improvements and Fixes

* Added a "Save All" action.
* Added an icon scale slider for the environment panel. @igorsaux
* Quick Edit menu options (nudge/dir) can now be edited with a mouse wheel.
* The search feature now supports searching by types.
* Actions for searching by type or prefab ID were added to the tile context menu.
* Minor GUI improvements to the search panel.

# v2.4.0.alpha

### Improvements and Fixes

* When filtering child types in the environment tree, a dash symbol will appear when the node is visible.
* Tile context menu now shows coordinates of the currently viewed tile.

# v2.3.0.alpha

### Screenshots!

A tool for taking map screenshots is available. Find it in the new settings menu for each map. To create a screenshot, select a directory, and press the appropriate button. The resulting image will respect filtered types and multi-z rendering if enabled.

### Replace Tool!

Added the "Replace" tool. When enabled, it will replace the hovered instance with the selected prefab. Quickly toggle the tool by holding the R key.

### Improvements and Fixes

* Fixed self-update for Windows when StrongDMM executable was not on the C drive.
* Unnecessary updates can now be ignored.
* Added a preference to disable self-update checks on startup. (Not recommended)

# v2.2.0.alpha

### Improvements and Fixes

* Added an option to change map size. Open the new "Settings" menu, under the cog button in the top-right corner of the opened map pane.
* Interaction with the map window is now possible without first manually tabbing in, specifically for: select instance -> change a var -> select another instance.
* Added a preference to adjust the application framerate.
* Added preferences to control the Quick Edit menu appearance: it can be shown in the tile context menu or on the map pane.
* Fixed issues with attempting to pick/delete items pixel-shifted off the map.
* Minor GUI improvements.

# v2.1.0.alpha

### Support StrongDMM!

If you enjoy StrongDMM and want to support the author, now you can! Find links to the support page in the editor and on the official GitHub page. Any form of donation is welcome. Your support motivates further development of StrongDMM!

### Improvements and Fixes

* Added an option to create new maps. Find the button in the menu: File->New Map; @LetterN
* Minor GUI improvements.
* Fixed shortcut helpers for context menus. @Arthur-Holiday
* Fixed a crash on Linux when opening an environment.

# v2.0.0.alpha

### Disclaimer!

This version is not a simple update, but a full rewrite from scratch. So, the "alpha" suffix means what it means.<br>
Some features may be absent, some bugs may exist. Feel free to post founded issues on GitHub.

### New GUI!

A new user interface became more "classic". Panels are now docked and by default there are three parts of the screen.

* Left: contains an environment tree;
* Right: here you can find everything about objects, their variables and so on;
* Central: a workspace area with all major windows (opened maps, preferences etc.).

One of the sweetest part of the new GUI is that all panels are movable! And their state persists between startups!<br>
Basically, you can drag any window to any side of the screen and dock it wherever you want.<br>
That is also applies for maps! You can open different map files and dock one to another.<br>
Thus, you'll see and edit both of them at the same time. Try it with "Mirror Canvas Camera" feature to see the real magic.

### New editing experience!

The biggest problem of the default DM editor is the way how variables are edited. To do that you need to open a modal dialog.<br>
Which is pretty annoying when you need to change a lot of stuff. The previous StrongDMM version repeated that approach.<br>
It had more features, yet the issue has not been solved.

Version 2 introduces a totally new approach: variables are always available! Without additional dialogs!<br>
You can select a prefab or a specific instance and modify their state right away.<br>
Because of that feature there is no need in a separate "preview" panel, which was a thing for a previous version.

### New shortcuts and tools!

There are separate tools to pick and to delete instances. They are a replacement for the old selection/deletion style.<br>
For example, instead of doing "Shift+LMB" (select hovered instance) you just hold "S" key. Same for instance deletion: hold "D" key.<br>
The new approach is simple and straightforward! Hover tools icons to see their tooltips.

### Other Improvements

* Multi-Z Rendering: If a map file contains multiple Z-levels, you can now view activities on the upper level that occur on the lower level.
* Auto-Update Feature: The editor now includes an auto-update feature, making the sdmmlauncher obsolete and deprecated.
* Types Filter in the Environment Tree: The environment tree now includes a types filter. Activate it by using the "eye" button or pressing "F" when the tree is focused.
* No Key Re-Arrangement on Save: The editor will no longer rearrange keys upon saving.
* New Search Window with Redesigned Results: A new search window features redesigned search results. Quick searches can be performed using "Ctrl+F."
* Quick Edit Window for Nudges and Dirs: A panel is available in the bottom-right corner of the canvas for faster modifications of nudges and dirs variables.

More updates are on the way...
