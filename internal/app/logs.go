package app

import (
	"os"
	"path/filepath"
	"sdmm/internal/util"
	"time"

	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"
)

func initializeLogs(internalDir string) string {
	// Configure logs directory.
	logDir := internalDir + "/logs"
	_ = os.MkdirAll(logDir, os.ModePerm)

	// Create log file for the current session.
	formattedDate := time.Now().Format(util.TimeFormat)
	logFile := logDir + "/" + formattedDate + ".log"
	file, err := os.OpenFile(logFile, os.O_CREATE|os.O_APPEND|os.O_WRONLY, os.ModePerm)
	if err != nil {
		panic("unable to open log file")
	}

	// Attach log output to the log file and an application terminal.
	fileWriter := zerolog.ConsoleWriter{
		Out:        file,
		TimeFormat: time.DateTime,
		NoColor:    true,
	}
	consoleWrite := zerolog.ConsoleWriter{
		Out:        os.Stdout,
		TimeFormat: time.DateTime,
	}

	multi := zerolog.MultiLevelWriter(fileWriter, consoleWrite)
	logger := zerolog.New(multi).With().Caller().Timestamp().Logger()
	log.Logger = logger

	return filepath.FromSlash(logDir)
}
