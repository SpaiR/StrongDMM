package dmmdata

import (
	"bufio"
	"errors"
	"fmt"
	"io"
	"strconv"

	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/dmapi/dmvars"
	"sdmm/internal/util"
)

func max(a, b int) int {
	if a >= b {
		return a
	}
	return b
}

type namedReader interface {
	io.Reader
	Name() string
}

// A slightly modified algorithm made on rust:
// https://raw.githubusercontent.com/SpaceManiac/SpacemanDMM/5e51421/src/tools/dmm/read.rs
// Unlike the original one, doesn't care about storing keys as base52 number and uses a simple string for that.
func parse(file namedReader) (*DmmData, error) {
	r := bufio.NewReader(file)

	var (
		// Variables:
		dmmData = DmmData{
			Filepath:   file.Name(),
			Dictionary: make(DataDictionary),
			Grid:       make(DataGrid),
			LineBreak:  "\n",
		}

		lineNo = 1

		inCommentLine  bool
		commentTrigger bool
		inQuoteBlock   bool
		inVarDataBlock bool
		inKeyBlock     bool
		inDataBlock    bool
		inVarEditBlock bool
		afterDataBlock = true
		escaping       bool

		currData      Prefabs
		currPath      = ""
		currVariables = &dmvars.MutableVariables{}
		currVar       = make([]rune, 0)
		currDatum     = make([]rune, 0)

		currKey []rune

		// Functions:
		flushCurrPrefab = func() {
			currData = append(currData, dmmprefab.New(dmmprefab.IdNone, currPath, currVariables.ToImmutable()))
			currPath = ""
			currVariables = &dmvars.MutableVariables{}
		}
		flushCurrPath = func() {
			currPath = string(currDatum)
			currDatum = currDatum[:0]
		}
		flushCurrVariable = func() {
			currVariables.Put(string(currVar), string(currDatum))
			currVar = currVar[:0]
			currDatum = currDatum[:0]
		}
		lineErr = func(msg string, args ...any) error {
			args = append([]any{1}, args...)
			args[0] = lineNo
			return fmt.Errorf("at line %d: "+msg, args...)
		}
	)

	for {
		if c, _, err := r.ReadRune(); err != nil {
			if errors.Is(err, io.EOF) {
				break
			} else {
				return nil, err
			}
		} else {
			if c == '\n' || c == '\r' {
				if c == '\n' {
					lineNo++
				} else {
					dmmData.LineBreak = "\r\n"
				}
				inCommentLine = false
				commentTrigger = false
				continue
			} else if inCommentLine {
				continue
			} else if c == ' ' || c == '\t' {
				if commentTrigger {
					return nil, lineErr("expected comment or type, got whitespace")
				}
				if inQuoteBlock {
					if c == '\t' {
						currDatum = append(currDatum, '\\', 't')
					} else {
						currDatum = append(currDatum, c)
					}
				}
				else if inVarDataBlock { // retain any whitespace in the data block
					currDatum = append(currDatum, c)
				}
				continue
			}

			if c == '/' && !inQuoteBlock {
				if commentTrigger {
					inCommentLine = true
					// If the first line and it's a comment, then we make an assumption that it's a TGM format.
					if lineNo == 1 {
						dmmData.IsTgm = true
					}
					continue
				} else {
					commentTrigger = true
				}
			} else {
				commentTrigger = false
			}

			if inDataBlock {
				if inVarEditBlock {
					if inQuoteBlock {
						currDatum = append(currDatum, c)
						if escaping {
							escaping = false
						} else if c == '\\' {
							escaping = true
						} else if c == '"' {
							inQuoteBlock = false
						}
					} else {
						if c == '"' {
							currDatum = append(currDatum, c)
							inQuoteBlock = true
						} else if c == '=' && len(currVar) == 0 {
							currVar = make([]rune, len(currDatum))
							copy(currVar, currDatum)
							currDatum = currDatum[:0]
						} else if c == ';' {
							flushCurrVariable()
						} else if c == '}' {
							if len(currVar) > 0 {
								flushCurrVariable()
							}
							inVarEditBlock = false
						} else if c == '(' && !inVarDataBlock { //list() parsing
							inVarDataBlock = true
							currDatum = append(currDatum, c)
						} else if c == ')' && inVarDataBlock {
							inVarDataBlock = false
							currDatum = append(currDatum, c)
						} else {
							currDatum = append(currDatum, c)
						}
					}
				} else if c == '{' {
					flushCurrPath()
					inVarEditBlock = true
				} else if c == ',' {
					if len(currPath) == 0 && len(currDatum) > 0 {
						flushCurrPath()
					}
					flushCurrPrefab()
				} else if c == ')' {
					if len(currPath) == 0 && len(currDatum) > 0 {
						flushCurrPath()
					}
					flushCurrPrefab()
					key := Key(currKey)
					currKey = currKey[:0]
					data := make(Prefabs, len(currData))
					copy(data, currData)
					currData = currData[:0]
					dmmData.Dictionary[key] = data
					inDataBlock = false
					afterDataBlock = true
				} else {
					currDatum = append(currDatum, c)
				}
			} else if inKeyBlock {
				if c == '"' {
					inKeyBlock = false
					if dmmData.KeyLength != len(currKey) {
						if dmmData.KeyLength == 0 {
							dmmData.KeyLength = len(currKey)
						} else {
							return nil, lineErr("inconsistent key length: %d vs %d", dmmData.KeyLength, len(currKey))
						}
					}
				} else {
					currKey = append(currKey, c)
				}
			} else if c == '"' {
				if !afterDataBlock {
					return nil, lineErr("failed to start a data block")
				}
				inKeyBlock = true
				afterDataBlock = false
			} else if c == '(' {
				if afterDataBlock {
					currKey = currKey[:0]
					break
				} else {
					inDataBlock = true
				}
			}
		}
	}

	type axis int

	const (
		X axis = iota
		Y
		Z
	)

	var (
		readingAxis = X

		currX, currY, currZ = 0, 0, 0
		currNum             = 0
		baseX               = 0

		inCoordBlock = true
		inMapString  = false

		finishLine = func() error {
			if len(currKey) != 0 {
				return lineErr("extra characters at EOL [%s]", string(currKey))
			}
			if currX != baseX {
				currY++
				dmmData.MaxX = max(dmmData.MaxX, currX-1)
				currX = baseX
			}
			return nil
		}
	)

	for {
		if c, _, err := r.ReadRune(); err != nil {
			if errors.Is(err, io.EOF) {
				break
			} else {
				return nil, err
			}
		} else {
			if inCoordBlock {
				if c == ',' {
					if readingAxis == X {
						currX = currNum
						currNum = 0
						baseX = currX
						readingAxis = Y
					} else if readingAxis == Y {
						currY = currNum
						currNum = 0
						readingAxis = Z
					} else {
						return nil, lineErr("incorrect number of axis [%d]", readingAxis)
					}
				} else if c == ')' {
					if readingAxis != Z {
						return nil, lineErr("incorrect reading axis [%d] (expected %d)", readingAxis, Z)
					}
					currZ = currNum
					currNum = 0
					dmmData.MaxZ = max(dmmData.MaxZ, currZ)
					inCoordBlock = false
					readingAxis = X
				} else {
					x, err := strconv.ParseInt(string(c), 10, 16)
					if err != nil {
						return nil, lineErr("%w", err)
					}
					currNum = 10*currNum + int(x)
				}
			} else if inMapString {
				if c == '"' {
					if err := finishLine(); err != nil {
						return nil, err
					}
					inMapString = false
					dmmData.MaxY = max(dmmData.MaxY, currY-1)
				} else if c == '\r' {
					dmmData.LineBreak = "\r\n" // Windows line break for sure.
				} else if c == '\n' {
					if err := finishLine(); err != nil {
						return nil, err
					}
					lineNo++
				} else {
					currKey = append(currKey, c)
					if len(currKey) == dmmData.KeyLength {
						dmmData.Grid[util.Point{X: currX, Y: currY, Z: currZ}] = Key(currKey)
						currKey = currKey[:0]
						currX++
					}
				}
			} else if c == '(' {
				inCoordBlock = true
			} else if c == '"' {
				inMapString = true
			} else if c == '\n' {
				lineNo++
			}
		}
	}

	// Make Y axis to go from bottom to top
	reversedGrid := make(DataGrid, len(dmmData.Grid))
	for z := 1; z <= dmmData.MaxZ; z++ {
		for y := 1; y <= dmmData.MaxY; y++ {
			for x := 1; x <= dmmData.MaxX; x++ {
				reversedGrid[util.Point{
					X: x,
					Y: dmmData.MaxY + 1 - y,
					Z: z,
				}] = dmmData.Grid[util.Point{
					X: x,
					Y: y,
					Z: z,
				}]
			}
		}
	}
	dmmData.Grid = reversedGrid

	return &dmmData, nil
}
