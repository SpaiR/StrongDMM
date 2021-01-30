package dmm

type Instance struct {
	Id   uint64
	Path string
	Vars map[string]string
}

func newInstance(id uint64, path string, vars map[string]string) *Instance {
	instance := Instance{
		Id:   id,
		Path: path,
		Vars: vars,
	}

	return &instance
}
