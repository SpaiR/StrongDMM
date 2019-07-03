All native libraries and executables which you need to run application locally.
Only for Windows and Linux systems with x64 arch since I really don't see the point to support x32 systems.

Except of two files all is needed for lwjgl.
Two exceptional files are sdmmparser and sdmmparser.exe.
Executable itself is used to parse BYOND environment and save JSON representation of it.
Under the hood it uses SpacemanDMM library, which could be found by next link: https://github.com/SpaceManiac/SpacemanDMM

Source code for 'sdmmparser' executable could be found in 'sdmmparser_source' file.
