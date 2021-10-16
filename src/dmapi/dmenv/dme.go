package dmenv

import (
	"fmt"
	"path/filepath"
	"strings"

	"sdmm/dmapi/dmvars"
	"sdmm/third_party/sdmmparser"
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

	traverseTree0(objectTreeType, "", &dme)

	linkPathFamily(&dme, "/atom", "/datum")
	linkPathFamily(&dme, "/atom/movable", "/atom")
	linkPathFamily(&dme, "/area", "/atom")
	linkPathFamily(&dme, "/turf", "/atom")
	linkPathFamily(&dme, "/obj", "/atom/movable")
	linkPathFamily(&dme, "/mob", "/atom/movable")

	for _, object := range dme.Objects {
		if object.parent != nil {
			object.Vars.LinkParent(object.parent.Vars)
		}
	}

	return &dme, nil
}

func nameFromPath(path string, parentName string) string {
	if parentName == "" && len(path) > 1 {
		return path[strings.LastIndex(path, "/")+1:]
	}
	return parentName
}

func traverseTree0(root *sdmmparser.ObjectTreeType, parentName string, dme *Dme) {
	variables := dmvars.MutableVariables{}
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
	}

	if _, ok := variables.Value("name"); !ok {
		variables.Put("name", "\""+nameFromPath(root.Path, parentName)+"\"")
	}

	var children []string
	for _, child := range root.Children {
		children = append(children, child.Path)
		traverseTree0(&child, name, dme)
	}

	dme.Objects[root.Path] = &Object{
		env:            dme,
		Path:           root.Path,
		Vars:           variables.ToImmutable(),
		DirectChildren: children,
	}
}

func linkPathFamily(dme *Dme, t string, parentType string) {
	if object := dme.Objects[t]; object != nil {
		linkFamily0(dme, object, parentType)
	}
}

func linkFamily0(dme *Dme, object *Object, parentType string) {
	object.parent = dme.Objects[parentType]
	for _, child := range object.DirectChildren {
		linkFamily0(dme, dme.Objects[child], object.Path)
	}
}

func sanitizeVar(value string) string {
	if len(value) > 2 && strings.HasPrefix(value, "{\"") && strings.HasSuffix(value, "\"}") {
		value = value[1 : len(value)-1]
		return value
	}
	return value
}
