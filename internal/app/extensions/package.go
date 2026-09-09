package extensions

import (
	"archive/zip"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"io"
	"os"
	"path/filepath"
	"runtime"
	"strings"

	api "github.com/SpaiR/StrongDMM-extension-api"
	"github.com/hashicorp/go-hclog"
	"github.com/hashicorp/go-plugin"
	"github.com/rs/zerolog/log"
)

// newPackageClient opens a .sdmmext ZIP package, extracts it to a content-
// addressed cache directory, and starts its command for this platform.
func newPackageClient(packagePath, cacheRoot string) *client {
	hash, err := fileHash(packagePath)
	if err != nil {
		log.Warn().Err(err).Str("package", packagePath).Msg("unable to read extension package")
		return nil
	}
	dir := filepath.Join(cacheRoot, hash)
	manifestPath := filepath.Join(dir, "extension.json")
	if _, err = os.Stat(manifestPath); err != nil {
		if err = extractPackage(packagePath, dir); err != nil {
			log.Warn().Err(err).Str("package", packagePath).Msg("unable to extract extension package")
			return nil
		}
	}
	data, err := os.ReadFile(manifestPath)
	if err != nil {
		log.Warn().Err(err).Str("package", packagePath).Msg("unable to read extension manifest")
		return nil
	}
	var manifest manifest
	if json.Unmarshal(data, &manifest) != nil || manifest.ID == "" || manifest.APIVersion != api.ProtocolVersion {
		log.Warn().Str("package", packagePath).Msg("extension manifest is invalid or incompatible")
		return nil
	}
	commandPath := manifest.Commands[runtime.GOOS+"-"+runtime.GOARCH]
	if commandPath == "" || filepath.IsAbs(commandPath) || filepath.Dir(commandPath) == "." {
		log.Warn().Str("package", packagePath).Msg("extension has no command for this platform")
		return nil
	}
	binary := filepath.Join(dir, commandPath)
	if relative, err := filepath.Rel(dir, binary); err != nil || relative == ".." || strings.HasPrefix(relative, ".."+string(filepath.Separator)) {
		log.Warn().Str("package", packagePath).Msg("extension command path escapes its package")
		return nil
	}
	if _, err = os.Stat(binary); err != nil {
		log.Warn().Err(err).Str("package", packagePath).Msg("extension command is missing")
		return nil
	}
	process := plugin.NewClient(&plugin.ClientConfig{
		HandshakeConfig:  api.HandshakeConfig,
		Plugins:          plugin.PluginSet{api.PluginName: &api.Plugin{}},
		Cmd:              extensionCommand(binary),
		AllowedProtocols: []plugin.Protocol{plugin.ProtocolNetRPC},
		Logger:           hclog.New(&hclog.LoggerOptions{Name: manifest.ID, Output: os.Stderr}),
	})
	rpcClient, err := process.Client()
	if err != nil {
		log.Warn().Err(err).Str("extension", manifest.ID).Msg("unable to start extension")
		process.Kill()
		return nil
	}
	raw, err := rpcClient.Dispense(api.PluginName)
	if err != nil {
		log.Warn().Err(err).Str("extension", manifest.ID).Msg("extension did not provide its service")
		process.Kill()
		return nil
	}
	extension, ok := raw.(api.Extension)
	if !ok {
		log.Warn().Str("extension", manifest.ID).Msg("extension returned an incompatible service")
		process.Kill()
		return nil
	}
	log.Info().Str("extension", manifest.ID).Str("package", packagePath).Msg("extension loaded")
	return &client{manifest: manifest, process: process, extension: extension}
}

func fileHash(path string) (string, error) {
	file, err := os.Open(path)
	if err != nil {
		return "", err
	}
	defer file.Close()
	hash := sha256.New()
	if _, err = io.Copy(hash, file); err != nil {
		return "", err
	}
	return hex.EncodeToString(hash.Sum(nil)), nil
}

func extractPackage(packagePath, destination string) error {
	archive, err := zip.OpenReader(packagePath)
	if err != nil {
		return err
	}
	defer archive.Close()
	for _, entry := range archive.File {
		entryPath := filepath.FromSlash(strings.ReplaceAll(entry.Name, "\\", "/"))
		if filepath.IsAbs(entryPath) {
			continue
		}
		path := filepath.Join(destination, entryPath)
		relative, relErr := filepath.Rel(destination, path)
		if relErr != nil || relative == "." || relative == ".." || strings.HasPrefix(relative, ".."+string(filepath.Separator)) {
			continue
		}
		if entry.FileInfo().IsDir() {
			if err = os.MkdirAll(path, os.ModePerm); err != nil {
				return err
			}
			continue
		}
		if err = os.MkdirAll(filepath.Dir(path), os.ModePerm); err != nil {
			return err
		}
		input, err := entry.Open()
		if err != nil {
			return err
		}
		output, err := os.OpenFile(path, os.O_CREATE|os.O_TRUNC|os.O_WRONLY, entry.Mode())
		if err != nil {
			input.Close()
			return err
		}
		_, copyErr := io.Copy(output, input)
		closeErr := output.Close()
		input.Close()
		if copyErr != nil {
			return copyErr
		}
		if closeErr != nil {
			return closeErr
		}
	}
	return nil
}
