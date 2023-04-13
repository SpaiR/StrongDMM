# Strong Dream Map Maker &middot; [![GitHub release](https://img.shields.io/github/release/SpaiR/StrongDMM.svg?label=StrongDMM)](https://github.com/SpaiR/StrongDMM/releases/latest) [![Github All Releases](https://img.shields.io/github/downloads/SpaiR/StrongDMM/total.svg?logo=github)](https://github.com/SpaiR/StrongDMM/releases) ![CI](https://github.com/SpaiR/StrongDMM/workflows/CI/badge.svg)

<p align="center"><b>Download StrongDMM</b></p>
<p align="center">
  <a href="https://bit.ly/sdmm-windows">
    <img src="https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white" alt="Windows download link"/>
  </a>
  <a href="https://bit.ly/sdmm-linux">
    <img src="https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black" alt="Linux download link"/>
  </a>
  <a href="https://bit.ly/sdmm-macos">
    <img src="https://img.shields.io/badge/mac%20os-000000?style=for-the-badge&logo=apple&logoColor=white" alt="macOS download ink"/>
  </a>
</p>

---

<img align="right" width="150" src="https://raw.githubusercontent.com/SpaiR/StrongDMM/master/docs/sdmm-logo.png" alt="StrongDMM Logo">

StrongDMM is an alternative yet robust map editor for BYOND.

It was built with the idea to create a more flexible, fast and extensible tool, than BYOND built-in map editor. The
editor has same features DM has, but provides much more and improves general map editing experience.

## Features
The editor provides a bunch of new features:

* TGM support with built-in map merger (no need to use external scripts and pre-commit hooks);
* Almost instant environment open;
* Custom layers filter;
* Built-in screenshot tool;
* Smooth zoom-in/zoom-out;
* Robust "Search";
* Improved shortcuts;
* Robust variables editor and variables preview;
* Optional variables sanitizing;
* Open with CLI.

...and a lot more...

<p align="center">
  <img width="450" src="https://raw.githubusercontent.com/SpaiR/StrongDMM/master/docs/sdmm-example.png" alt="StrongDMM Example">
</p>

## How to Use

StrongDMM is a single executable, which doesn't require any installation.
You can download it by any of the provided link and start it right away. 

**Download Links:**

* [Windows](https://bit.ly/sdmm-windows)
* [Linux](https://bit.ly/sdmm-linux)
* [macOS](https://bit.ly/sdmm-macos)

[Release](https://github.com/SpaiR/StrongDMM/releases/latest) page contains all distributed files. It also has `sha256` hashes info for every executable for validation purposes.

### CLI Usage

StrongDMM do support CLI to quickly open maps. Provide `.dme` or `.dmm` files as program arguments:

###### With DME
```
strongdmm.exe path/to/environment.dme ./map1.dmm ../path/map2.dmm
```

###### Without DME
```
strongdmm.exe ./map1.dmm ../path/map2.dmm
```

When providing `.dmm` files without `.dme`, a proper environment file will be found automatically.

## Support
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/P5P5BF17Q)

StrongDMM developed without any monetization in mind. The main motivation is an enthusiasm to do a cool stuff.\
Your support can show your gratefulness and will motivate the project further development.

Also, if you want something specific to be implemented in the editor, it's possible to focus on your needs.\
Feel free to reach me out with my public contacts to discuss details:
* [E-Mail](mailto:despsolver@gmail.com)
* [Discord](https://discordapp.com/users/153940096389742592)

## FAQ

**Q.** My antivirus software detects something suspicious in the editor binaries. Is it ok?\
**A.
** Yes. It's a false positive reaction to the way how Golang (development language) creates binaries. Read more: https://go.dev/doc/faq#virus

**Q.** How do I verify my executables?\
**A.** With `sha256` hashes available on the [release](https://github.com/SpaiR/StrongDMM/releases/latest) page.

**Q.** But how can I trust executables on the release page?\
**A.
** Executables are built with the [CI](https://github.com/SpaiR/StrongDMM/actions/workflows/ci.yml) pipeline. You can verify the process by yourself or build executables manually from the source code.

**Q.** How to uninstall the editor?\
**A.
** StrongDMM doesn't have any installation. Thus, no specific "uninstall" required as well. Delete the executable and, if you want to remove editor data, its directory on OS.

**Q.** Where do I find editor data?\
**A.** For Windows: `C:\Users\USER\AppData\Roaming\StrongDMM`, for Linux/macOS: `~/.strongdmm`.

**Q.** How to move the map?\
**A.** ~~Use zoom-in/zoom-out.~~ Drag the pan with the **middle mouse button**. Or by holding a **space key**.
Alternatively you can use **arrow keys**.

**Q.** How to zoom?\
**A.** Scroll your mouse or use **+/- keys** on the keyboard.

**Q.** How to change a save format?\
**A.** In the menu bar: `File->Preferences...` and change to the format you need.

**Q.** The editor crushed. Where I can find logs?\
**A.** Menu button: `Help->Open Logs Folder`.

## How to Build

There are two steps to build the application:

1. Build **sdmmparser** library;
2. Build the editor.

**sdmmparser** is a rust library based on [SpacemanDMM](https://github.com/SpaceManiac/SpacemanDMM) parser. It is
compiled to a `staticlib`. You can find it by path: `internal/third_party/sdmmparser/src`.

### Pre-requests

* [Go](https://go.dev/): version **1.20** or higher
* [Rust](https://www.rust-lang.org/): version **1.56.0** or higher
* (Optional) [Task](https://taskfile.dev): to run build scripts

#### For Windows

Ensure your Rust configured to use `stable-x86_64-pc-windows-gnu` toolchain. That requirement is based on the thing,
that Go can't use MSVC to do builds. Thus, it won't link with libraries built using anything but GNU.

The easiest way to get GNU compiler is to install MinGW. This could be done with many ways: chocolatey package, msys2
etc.

#### For Linux

You would probably need to add dependencies to build GUI apps: `sudo apt install xorg-dev libgtk-3-dev`.

### Steps

#### Using Task

Task is a cross-platform alternative for Make. Its scripts are described in the `Taskfile.yml`.

With installed Task you can basically do:

* `task build`: to build sdmmparser + the editor (result will be in the `dst` dir)
* `task run`: to run the editor (will do a compilation under the hood)

#### Manually

1. The important part is to build **sdmmparser** library.\
   Go to the `internal/third_party/sdmmparser/src` directory and run the command: `cargo build --release`.\
   If you don't modify **sdmmparser** you can do that step only once.
2. In the root you can do:
    * `go build .`: to build the editor (executable will be in the root with the name `sdmm.exe`/`sdmm`)
    * `go run .`: to run the editor

## Credits

StrongDMM uses [SpacemanDMM](https://github.com/SpaceManiac/SpacemanDMM) parser made
by [SpaceManiac](https://github.com/SpaceManiac). \
The application icon is designed by [Clément "Topy"](https://github.com/clement-or).

## License

See the LICENSE file for license rights and limitations (GPL-3.0).
