# Strong Dream Map Maker &middot; [![GitHub release](https://img.shields.io/github/release/SpaiR/StrongDMM.svg?label=StrongDMM)](https://github.com/SpaiR/StrongDMM/releases/latest) ![CI Build](https://github.com/SpaiR/StrongDMM/workflows/CI%20Build/badge.svg)

<p align="center">
  <a href="https://github.com/SpaiR/sdmmlauncher/releases/latest/download/sdmmlauncher.exe">
    <img src="http://img.shields.io/badge/Download%20For%20Windows-0078D7?style=for-the-badge"/>
  </a>
  <a href="https://github.com/SpaiR/sdmmlauncher/releases/latest/download/sdmmlauncher">
    <img src="http://img.shields.io/badge/Download%20For%20Linux-E95420?style=for-the-badge"/>
  </a>
</p>

<img align="right" width="150" src="https://raw.githubusercontent.com/SpaiR/StrongDMM/master/docs/sdmm-logo.png">

StrongDMM is an alternative yet robust map editor for BYOND. 

It was built with idea to create a more flexible, fast and extensible tool, than BYOND built-in map editor. The editor has same features DM has, but provides much more and improves general map editing experience.

## Features

The editor provides a bunch of new features:
 * TGM support with built in map merger (no need to use external scripts and pre-commit hooks);
 * Almost instant environment open;
 * Custom layers filter;
 * Built-in screenshot tool;
 * Smooth zoom-in/zoom-out;
 * Robust "Search&Replace";
 * Improved shortcuts;
 * Robust variables editor and variables preview;
 * Optional variables sanitizing;

...and a lot more...

<p align="center">
  <img width="450" src="https://raw.githubusercontent.com/SpaiR/StrongDMM/master/docs/sdmm-example.png">
</p>

## How to Use
The recommended way is to use `sdmmlauncher`. Launcher will ensure that you are using the up to date version of the editor.

**Download links:**
* [Windows](https://github.com/SpaiR/sdmmlauncher/releases/latest/download/sdmmlauncher.exe)
* [Linux](https://github.com/SpaiR/sdmmlauncher/releases/latest/download/sdmmlauncher)

You are also able to download the editor manually. Go to the [release](https://github.com/SpaiR/StrongDMM/releases/latest)
page and download zip for your OS.

## Usage FAQ
**Q.** Launcher downloaded the latest version and nothing happens.<br>
**A.** Start the launcher as an administrator, check your username doesn't contains any non-latin chars.

**Q.** I'm experiencing an eternal environment parsing. What to do?<br>
**A.** Start the editor (launcher or a proper script file) **as an administrator**.

**Q.** How to move the map?<br>
**A.** ~~Use zoom-in/zoom-out.~~ Drag and drop while holding down the **middle mouse button**. 
Or by holding the **space bar** in combination with the **left mouse button**. Or **arrow keys**.

**Q.** How to change a save format?<br>
**A.** In the menu bar: `File->Preferences...` and change the format you would like to use.

**Q.** The editor went crush. Where I can find the error logs?<br>
**A.** In your user home dir: `.strongdmm/logs`.

## How to Build
**Pre-requests:** [JDK 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) or higher, latest
[Rust](https://www.rust-lang.org/) to compile `sdmmparser`

#### Editor:
- **Build:** `gradlew clean build`
- **Run:** `gradlew runShadow`
- **Auto-format (for linter)**: `gradlew formatKotlin`

#### sdmmparser (Optional):
- **Build**: `cargo build`
- To build a release (optimized) version just add `--release` flag in the end of the command.

## Credits
StrongDMM uses [SpacemanDMM](https://github.com/SpaceManiac/SpacemanDMM) parser made by [SpaceManiac](https://github.com/SpaceManiac).<br>
The application icon is designed by [Clément "Topy"](https://github.com/clement-or).<br>
The editor took some initial ideas from [FastDMM](https://github.com/monster860/FastDMM) made by [monster860](https://github.com/monster860)
and later supported by [/tg/station](https://github.com/tgstation/FastDMM).

## License
See the LICENSE file for license rights and limitations (GPL-3.0).
