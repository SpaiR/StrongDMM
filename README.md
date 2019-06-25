[![Build Status](https://travis-ci.org/SpaiR/StrongDMM.svg?branch=master)](https://travis-ci.org/SpaiR/StrongDMM)

## Strong Dream Map Maker
Yet another robust map editor for BYOND.


### What is it
StrongDMM is an alternative map editor for BYOND. It was built with thoughts to create more flexible, fast and extensible instrument,
than out of the box editor. Under the hood, StrongDMM uses [SpacemanDMM](https://github.com/SpaceManiac/SpacemanDMM) parser so the process of opening
dme project takes several seconds instead of eternity, which BYOND editor provides.

From the other point, StrongDMM doesn't make mapping experience absolutely different. Familiar hotkeys, similar editing behaviour,
all of that was made to make the process of migrating to the new editor fast and smooth.


### Features
* Usage of [SpacemanDMM](https://github.com/SpaceManiac/SpacemanDMM) parser to parse environment. That means that StrongDMM has full support
of sophisticated BYOND syntax and can work with stuff like definition through macros, etc.
* Much specific display filters! Separate menu with which you can hide and show exactly what you want.
    * Example: [GIF](https://imgur.com/a/e8apLGT)
* Built in map merger. This thing will minimize changes in file, so resulted diff won't be too big. The algorithm itself is similar that uses TG codebase.
* Map file cleanings. StrongDMM will sanitize variables which declared for specific instance on map, but have the same value in environment.
That means that all instances which were made by providing same variable as in code, will be deleted from the map and replaced with initial object.
* Simplified way of looking through the instances. Now you can view instances variables in selection menu.
    * Example: [PNG](https://imgur.com/a/PykpCmw)
* Improved variables editing. Searching and ability to show only instance variables.
    * Example: [GIF](https://imgur.com/a/ew38gYU)
* Map synchronization. You can open different maps in different files and work with them like if they are in the same file, but on different z-levels.
* TGM support out of the box. You don't need to manually convert map after editing.

**Disclaimer!**<br>
StrongDMM doesn't support multi-z map files. It was done on purpose. Multi-z maps are absolutely unmaintainable from the perspective of Open Source contribution.
Divide your z-levels in different files and use **Map Synchronization** option.


### How to install
Open [releases](https://github.com/SpaiR/StrongDMM/releases) page and download distribution package you want.
No dependencies required, all packages are fully self containable.


### Credits
Thanks to original [FastDMM](https://github.com/monster860/FastDMM) made by [monster860](https://github.com/monster860)
and later supported by [TG](https://github.com/tgstation/FastDMM). A lot of good ideas were taken from there.<br>
Special thanks to [SpaceManiac](https://github.com/SpaceManiac) for his amazing [SpacemanDMM](https://github.com/SpaceManiac/SpacemanDMM) parser.
This guy makes BYOND development better.


### License
See the LICENSE file for license rights and limitations (GPL-3.0).
