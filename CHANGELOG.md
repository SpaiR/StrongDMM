# v2.8.1.alpha

Fixed crash in the recent "Move Tool".

# v2.8.0.alpha

* Added border-fill mode for the Fill Tool, accessible by holding CTRL. @mc-oofert
* Find On Map, now starts a search by path. @mc-oofert
* Added the Move Tool, which allows you to move a singular object. @mc-oofert
* Added a basic autocorrect function to variable editing, which automatically encloses strings and lists @mc-oofert
* Added a "Screenshot in Selection" checkbox to the Screenshot utility, which allows you to only screenshot the selected area by the Grab Tool. @mc-oofert

# v2.7.4.alpha

Fixed: whitespace duplication while parsing lists. @LemonInTheDark

# v2.7.3.alpha

Fixed: list whitespace parsing. @itsmeow

# v2.7.2.alpha

* Fix infinite scrolling when the canvas is exited during a scroll; @itsmeow
* Fix deletion processing out of bounds tiles. @itsmeow

# v2.7.1.alpha

Fixed: respecting of Z-level when searching object with a "Find on Map".

# v2.7.0.alpha

* Renamed save format: "DM" to "DMM" since it is more correct in terms of terminology;
* Improved environment parsing: linking between parent type and children happens with a specific parent_type var; @SpaceManiac
* Implemented support for editing "tmp, const, and static" vars: works in the same manner as it's done in DreamMaker. @SpaceManiac

# v2.6.2.alpha

Fixed: area bounding size for the Grab tool was 1 less.

# v2.6.1.alpha

* Revert: renamed "New Map" button to "Create Map" and moved to the "Edit" menu;
* When selecting multiple maps to open in the available maps pane, open button will show in the tooltip only a part of those maps.

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

* "Options" menu renamed to "View";
* Renamed "New Map" button to "Create Map" and moved to the "Edit" menu;
* Removed "Recent Environments" menu button;
* Added "Close Environment" menu button;
* Environment tree will respect prefab "color" variable;
* Slightly improved "Grab" tool performance;
* Improved map parsing algorithm (now shows lines with an error); @d0sboots
* Added a preference option to enable/disable auto-updates (enabled by default);
* Added status indicators for the "Grab" tool: size and area bounds coordinates;
* Added information dialog when loading environment;
* Fixed invalid shortcuts handling with custom layout.

# v2.5.4.alpha

Fixed crashes when modifying maps with specific sizes. The issue was connected with internal chunks generation.<br>
For maps with axis sizes less than a chunk size they were generated improperly.

# v2.5.3.alpha

### Fixes

* Fixed rare crashes when deleting instances;
* Updated build dependencies. [maintenance]

# v2.5.2.alpha

Fixed "option" preference wrong options. @KIBORG04

# v2.5.1.alpha

Fixed keypad "enter" key didn't work. @igorsaux

# v2.5.0.alpha

### Improvements and Fixes

* Added a "Save All" action;
* Added an icon scale slider for the environment panel; @igorsaux
* Options in Quick Edit menu (nudge/dir) now can be edited with a mouse wheel;
* Search feature now supports searching by types;
* Actions to search by type or prefab ID were added to the tile context menu;
* Minor GUI improvements to a search panel.

# v2.4.0.alpha

### Improvements and Fixes

* While filtering child types in the environment tree, there will be a dash symbol when the node is visible;
* Tile context menu now shows coords of the currently viewed tile.

# v2.3.0.alpha

### Screenshots!

Tool to make maps screenshots is available. You can find it in the new settings menu for every map specifically.
To create a screenshot select a directory where you want to create it and press an appropriate button.
Resulted image will respect filtered types and multi-z rendering if enabled.

### Replace Tool!

"Replace" tool has been added. When enabled, it wll replace the clicked hovered instance with the selected prefab.
Tool has a quick shortcut to toggle: hold R key.

### Improvements and Fixes

* Fixed self-update for Windows when StrongDMM executable was not on the C drive;
* Unnecessary updates can now be ignored;
* Added a preference to disable self-update check on startup. (Unrecommended)

# v2.2.0.alpha

### Improvements and Fixes

* Added an option to change map size. To do that open a new "Settings" menu, available under the cog button in the top-right corner of the opened map pane;
* It's now possible to interact with the map window without first having to manually tab in. Specifically for: select instance -> change a var -> select another instance;
* Added a preference to adjust the application framerate;
* Added preferences to control Quick Edit menu appearance: it can be shown in the tile context menu or on the map pane;
* Fixed attempting to pick/delete item pixel shifted off map does not work;
* Minor GUI improvements.

# v2.1.0.alpha

### Support StrongDMM!

If you like StrongDMM and really want to show that to the author, now you can do that!
Appropriate links to the support page can be found in the editor and official GitHub page.
Feel free to make any sort of donation. Your support will motivate further StrongDMM development!

### Improvements and Fixes

* Added an option to create new maps. The button can be found in menu: File->New Map; @LetterN
* Minor GUI improvements;
* Fixed shortcuts helpers for a context menu; @Arthur-Holiday
* Fixed crush for Linux while opening environment.

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

* Multi-Z rendering is a thing. If one map file contains multiple z-levels, you can see on the upper level what happens on the lower.
* Auto-update is a part of the editor. This means, that sdmmlauncher is unneeded and deprecated.
* Types filter has been integrated in the environment tree. To enable it uses the "eye" button or "F" hotkey, when the tree is focused.
* The editor won't do keys re-arrangement on save.
* New search window with redesigned search results. You can use "Ctrl+F" to do a quick search.
* Quick Edit window for nudges and dirs. In the bottom-right corner of the canvas you can find a panel, to modify those vars faster.

And there more to come...
