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

---

# v2.0.0.alpha

### Disclaimer!
This version is not a simple update, but a full rewrite from scratch. So, the "alpha" suffix means what it means.<br>
Some features may be absent, some bugs may exist. Feel free to post founded issues on GitHub.

## Major Improvements

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
It had more features, yet the problem has not been solved.

Version 2 introduces a totally new approach: variables are always available! Without additional dialogs!<br>
You can select a prefab or a specific instance and modify their state right away.<br>
Because of that feature there is no need in a separate "preview" panel, which was a thing for a previous version.

### New shortcuts and tools!
There are separate tools to pick and to delete instances. They are a replacement for the old selection/deletion style.<br>
For example, instead of doing "Shift+LMB" (select hovered instance) you just hold "S" key. Same for instance deletion: hold "D" key.<br>
The new approach is simple and straightforward! Hover tools icons to see their tooltips.

## Other Improvements
 * Multi-Z rendering is a thing. If one map file contains multiple z-levels, you can see on the upper level what happens on the lower.
 * Auto-update is a part of the editor. This means, that sdmmlauncher is unneeded and deprecated.
 * Types filter has been integrated in the environment tree. To enable it use the "eye" button or "F" hotkey, when the tree is focused.
 * The editor won't do keys re-arrangement on save.
 * New search window with redesigned search results. You can use "Ctrl+F" to do a quick search.
 * Quick Edit window for nudges and dirs. In the bottom-right corner of the canvas you can find a panel, to modify those vars faster.

And there more to come...
