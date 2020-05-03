[![GitHub release](https://img.shields.io/github/release/SpaiR/StrongDMM.svg?label=StrongDMM)](https://github.com/SpaiR/StrongDMM/releases/latest)
[![Build Status](https://travis-ci.org/SpaiR/StrongDMM.svg?branch=master)](https://travis-ci.org/SpaiR/StrongDMM)

# Strong Dream Map Maker
Yet another robust map editor for BYOND.

### What is it
StrongDMM is an alternative map editor for BYOND. It was built with thoughts to create a more flexible, fast and extensible tool,
than the native one.

The editor provides a bunch of new features:
 * TGM support with built in map merger (no need to use external scripts and pre-commit hooks)
 * Almost instant environment open
 * Smooth zoom-in/zoom-out
 * Robust "search&replace"
 * Improved shortcuts
 * Variables preview for the active object
 * Robust variables editor
 * Optional variables sanitizing

...and a lot of more...

### How to Use
The recommended way is to use `sdmmlauncher`. Launcher will ensure that you are using the up to date version of the editor.

**Download links:**
* [Windows](https://github.com/SpaiR/StrongDMM/releases/latest/download/sdmmlauncher.exe)
* [Linux](https://github.com/SpaiR/StrongDMM/releases/latest/download/sdmmlauncher)

You are also able to download the editor manually. Go to the [release](https://github.com/SpaiR/StrongDMM/releases/latest)
page and download zip for your OS.

### Usage FAQ
**Q.** I'm experiencing an eternal environment parsing. What to do?<br>
**A.** Start the editor (launcher or proper script file) **as an administrator**.

**Q.** How to move the map?<br>
**A.** ~~Use zoom-in/zoom-out.~~ Drag and drop while holding down the **middle mouse button**. 
Or by holding the **space bar** in combination with the **left mouse button**.

**Q.** How to change a save format?<br>
**A.** In the menu bar: `Options->Preferences...` and change the format you would like to use.

**Q.** How to copy a type of the currently selected object to the clipboard?<br>
**A.** Click on the title (blue line at the top) of the **objects window** with the right mouse button. 
You'll find a button named "Copy Type to Clipboard".

**Q.** The editor went crush. Where I can find the error logs?<br>
**A.** In your user home dir: `.strongdmm/logs`.

<hr>

### How to Build
**Pre-requests:** [JDK 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) or higher, latest
[Rust](https://www.rust-lang.org/) to compile `sdmmparser` and `sdmmlauncher`.

#### Editor:
- **Build:** `gradlew clean build`
- **Run:** `gradlew runShadow`
- **Auto-format (for linter)**: `gradlew formatKotlin`

#### sdmmparser/sdmmlauncher (Optional):
- **Build**: `cargo build`
- To build a release (optimized) version just add `--release` flag in the end of the command.

### Credits
StrongDMM uses [SpacemanDMM](https://github.com/SpaceManiac/SpacemanDMM) parser made by [SpaceManiac](https://github.com/SpaceManiac).<br>
The application icon is designed by [Clément "Topy"](https://github.com/clement-or).<br>
Some of the initial ideas are taken from [FastDMM](https://github.com/monster860/FastDMM) made by [monster860](https://github.com/monster860)
and later supported by [/tg/station](https://github.com/tgstation/FastDMM).

### License
See the LICENSE file for license rights and limitations (GPL-3.0).
