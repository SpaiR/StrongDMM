// sdmmpack packages prebuilt extension commands into a .sdmmext archive.
package main

import (
	"archive/zip"
	"encoding/json"
	"flag"
	"io"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
)

type manifest struct {
	Commands map[string]string `json:"commands"`
}

func main() {
	source := flag.String("source", ".", "extension source directory")
	manifestPath := flag.String("manifest", "extension.json", "extension manifest")
	output := flag.String("output", "extension.sdmmext", "output package")
	build := flag.Bool("build", false, "build commands declared by the manifest before packaging")
	flag.Parse()
	data, err := os.ReadFile(*manifestPath)
	if err != nil {
		panic(err)
	}
	var extension manifest
	if err = json.Unmarshal(data, &extension); err != nil {
		panic(err)
	}
	if *build {
		for platform, command := range extension.Commands {
			goos, goarch, ok := strings.Cut(platform, "-")
			if !ok || goos == "" || goarch == "" {
				panic("invalid platform " + platform)
			}
			if command == "" || filepath.IsAbs(command) || filepath.Dir(command) == "." {
				panic("invalid command for " + platform)
			}
			path := filepath.Join(*source, command)
			if err = os.MkdirAll(filepath.Dir(path), 0755); err != nil {
				panic(err)
			}
			buildCommand := exec.Command("go", "build", "-o", path, ".")
			buildCommand.Dir = *source
			buildCommand.Env = append(os.Environ(), "GOOS="+goos, "GOARCH="+goarch)
			buildCommand.Stdout = os.Stdout
			buildCommand.Stderr = os.Stderr
			if err = buildCommand.Run(); err != nil {
				panic(err)
			}
		}
	}
	stage, err := os.MkdirTemp("", "sdmmpack-")
	if err != nil {
		panic(err)
	}
	defer os.RemoveAll(stage)
	for platform, command := range extension.Commands {
		if command == "" || filepath.IsAbs(command) || filepath.Dir(command) == "." {
			panic("invalid command for " + platform)
		}
		destination := filepath.Join(stage, command)
		if err = os.MkdirAll(filepath.Dir(destination), 0755); err != nil {
			panic(err)
		}
		copyFile(destination, filepath.Join(*source, command))
	}
	copyFile(filepath.Join(stage, "extension.json"), *manifestPath)
	if err = os.MkdirAll(filepath.Dir(*output), 0755); err != nil {
		panic(err)
	}
	archive, err := os.Create(*output)
	if err != nil {
		panic(err)
	}
	defer archive.Close()
	zipper := zip.NewWriter(archive)
	err = filepath.Walk(stage, func(path string, info os.FileInfo, walkErr error) error {
		if walkErr != nil || info.IsDir() {
			return walkErr
		}
		relative, _ := filepath.Rel(stage, path)
		entry, err := zipper.Create(filepath.ToSlash(relative))
		if err != nil {
			return err
		}
		file, err := os.Open(path)
		if err != nil {
			return err
		}
		defer file.Close()
		_, err = io.Copy(entry, file)
		return err
	})
	if err != nil {
		panic(err)
	}
	if err = zipper.Close(); err != nil {
		panic(err)
	}
}

func copyFile(destination, source string) {
	input, err := os.Open(source)
	if err != nil {
		panic(err)
	}
	defer input.Close()
	output, err := os.Create(destination)
	if err != nil {
		panic(err)
	}
	defer output.Close()
	if _, err = io.Copy(output, input); err != nil {
		panic(err)
	}
}
