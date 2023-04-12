package dmenv

import (
	"fmt"
	"path/filepath"
	"strings"

	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmvars"
	"sdmm/internal/third_party/sdmmparser"
)

type Dme struct {
	Name     string
	RootDir  string
	RootFile string
	Objects  map[string]*Object
}

func New(path string) (*Dme, error) {
	dme := Dme{
		Name:     filepath.Base(path),
		RootDir:  filepath.Dir(path),
		RootFile: path,
		Objects:  make(map[string]*Object),
	}

	objectTreeType, err := sdmmparser.ParseEnvironment(path)
	if err != nil {
		return nil, fmt.Errorf("[dmenv] unable to create dme by path [%s]: %w", path, err)
	}

	traverseTree0(objectTreeType, "", nil, &dme)

	for _, object := range dme.Objects {
		if parentType, ok := object.Vars.Value("parent_type"); ok {
			object.parent = dme.Objects[parentType]
		}
		if object.parent != nil {
			object.Vars.LinkParent(object.parent.Vars)
		}
	}

	return &dme, nil
}

func nameFromPath(path string, parentName string) string {
	if parentName == "" && len(path) > 1 {
		return "\"" + dm.PathLast(path) + "\""
	}
	return parentName
}

func traverseTree0(root *sdmmparser.ObjectTreeType, parentName string, parent *Object, dme *Dme) {
	variables := dmvars.MutableVariables{}
	varFlags := make(map[string]VarFlags, len(root.Vars))
	var name string

	for _, treeVar := range root.Vars {
		value := sanitizeVar(treeVar.Value)

		if treeVar.Name == "name" {
			if value == dmvars.NullValue {
				value = nameFromPath(root.Path, parentName)
			}

			name = value
		}

		variables.Put(treeVar.Name, value)

		if treeVar.Decl {
			var flags VarFlags
			flags.Tmp = treeVar.IsTmp
			flags.Const = treeVar.IsConst
			flags.Static = treeVar.IsStatic
			varFlags[treeVar.Name] = flags
		}
	}

	if _, ok := variables.Value("name"); !ok {
		variables.Put("name", nameFromPath(root.Path, parentName))
	}

	object := &Object{
		env:      dme,
		parent:   parent,
		Path:     root.Path,
		Vars:     variables.ToImmutable(),
		VarFlags: varFlags,
	}

	children := make([]string, 0, len(root.Children))
	for _, child := range root.Children {
		children = append(children, child.Path)
		traverseTree0(&child, name, object, dme)
	}

	object.DirectChildren = children
	dme.Objects[root.Path] = object
}

func sanitizeVar(value string) string {
	if len(value) > 2 && strings.HasPrefix(value, "{\"") && strings.HasSuffix(value, "\"}") {
		value = value[1 : len(value)-1]
		return value
	}
	return value
}
