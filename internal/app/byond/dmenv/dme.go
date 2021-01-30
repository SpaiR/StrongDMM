package dmenv

import (
	"fmt"
	"path/filepath"
	"strings"

	"github.com/SpaiR/strongdmm/internal/app/byond/dmvars"
	"github.com/SpaiR/strongdmm/third_party/sdmmparser"
)

type Dme struct {
	Name     string
	RootDir  string
	RootFile string
	Objects  map[string]*Object
}

func New(file string) (*Dme, error) {
	dme := Dme{
		Name:     filepath.Base(file),
		RootDir:  filepath.Dir(file),
		RootFile: file,
		Objects:  make(map[string]*Object),
	}

	objectTreeType, err := sdmmparser.ParseEnvironment(file)
	if err != nil {
		return nil, fmt.Errorf("unable to create dme by path [%s]: %w", file, err)
	}

	traverseTree0(objectTreeType, nil, &dme)

	linkPathFamily(&dme, "/atom", "/datum")
	linkPathFamily(&dme, "/atom/movable", "/atom")
	linkPathFamily(&dme, "/area", "/atom")
	linkPathFamily(&dme, "/turf", "/atom")
	linkPathFamily(&dme, "/obj", "/atom/movable")
	linkPathFamily(&dme, "/mob", "/atom/movable")

	for _, object := range dme.Objects {
		if object.parent != nil {
			object.Vars.SetParent(object.parent.Vars)
		}
	}

	return &dme, nil
}

func nameFromPath(path string, parentName *string) *string {
	if parentName == nil && len(path) > 1 {
		s := path[strings.LastIndex(path, "/")+1:]
		return &s
	} else {
		return parentName
	}
}

func traverseTree0(root *sdmmparser.ObjectTreeType, parentName *string, dme *Dme) {
	variables := dmvars.Variables{}
	var name *string

	for _, treeVar := range root.Vars {
		value := sanitizeVar(treeVar.Value)

		if treeVar.Name == "name" {
			if value == nil {
				value = nameFromPath(root.Path, parentName)
			}

			name = value
		}

		variables.Put(treeVar.Name, value)
	}

	if _, ok := variables.Value("name"); !ok {
		variables.Put("name", nameFromPath(root.Path, parentName))
	}

	var children []string
	for _, child := range root.Children {
		children = append(children, child.Path)
		traverseTree0(&child, name, dme)
	}

	dme.Objects[root.Path] = &Object{
		env:            dme,
		Path:           root.Path,
		Vars:           &variables,
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

func sanitizeVar(value string) *string {
	if len(value) > 2 && strings.HasPrefix(value, "{\"") && strings.HasSuffix(value, "\"}") {
		value = value[1 : len(value)-1]
		return &value
	} else if value == "null" {
		return nil
	}
	return &value
}
