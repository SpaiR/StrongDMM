package byond

import (
	"fmt"
	"path/filepath"
	"strings"

	"github.com/SpaiR/strongdmm/third_party/sdmmparser"
)

type Dme struct {
	Name         string
	RootDirPath  string
	RootFilePath string
	Items        map[string]*DmeItem
}

func NewDme(file string) (*Dme, error) {
	dme := Dme{
		Name:         filepath.Base(file),
		RootDirPath:  filepath.Dir(file),
		RootFilePath: file,
		Items:        make(map[string]*DmeItem),
	}

	objectTreeType, err := sdmmparser.ParseEnvironment(file)
	if err != nil {
		return nil, fmt.Errorf("unable to create dme by path [%s]: %w", file, err)
	}

	traverseTree0(objectTreeType, nil, &dme)

	return &dme, nil
}

func nameFromType(localType string, parentName *string) *string {
	if parentName == nil && len(localType) > 1 {
		s := localType[strings.LastIndex(localType, "/") + 1:]
		return &s
	} else {
		return parentName
	}
}

func traverseTree0(root *sdmmparser.ObjectTreeType, parentName *string, dme *Dme) {
	localVars := make(map[string]*string)
	var name *string

	for _, treeVar := range root.Vars {
		value := sanitizeVar(treeVar.Value)

		if treeVar.Name == "name" {
			if value == nil {
				value = nameFromType(root.Path, parentName)
			}

			name = value
		}

		localVars[treeVar.Name] = value
	}

	if _, ok := localVars["name"]; !ok {
		localVars["name"] = nameFromType(root.Path, parentName)
	}

	var children []string
	for _, child := range root.Children {
		children = append(children, child.Path)
		traverseTree0(&child, name, dme)
	}

	dme.Items[root.Path] = &DmeItem{
		env:      dme,
		Type:     root.Path,
		Vars:     localVars,
		Children: children,
	}
}

func sanitizeVar(value string) *string {
	if len(value) > 2 && strings.HasPrefix(value, "{\"") && strings.HasSuffix(value, "\"}") {
		value = value[1 : len(value)-1]
		return &value
	} else if value == "null" {
		return nil
	}
	return &value
}
