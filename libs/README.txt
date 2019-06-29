All native libraries which you need to run application locally.
Only for Windows and Linux systems with x64 arch since I really don't see the point to support x32 systems.

Except two files - all is needed for lwjgl.
Two exceptional files, libsdmmparser64.so and sdmmparser64.dll, are small libraries for Linux and Windows appropriately.
Library itself is used to parse BYOND environemt and get JSON representation of it to use it inside of editor.
Under the hood they are using SpacemanDMM library which could be found by next link: https://github.com/SpaceManiac/SpacemanDMM

Source code of 'sdmmparser64' library could be found in 'sdmmparser_source' file.
