package widget

type widget interface {
	Build()
}

type Layout []widget

func (layout Layout) Build() {
	for _, w := range layout {
		w.Build()
	}
}
