package widget

type customWidget struct {
	builder func()
}

func (c *customWidget) Build() {
	c.builder()
}

func Custom(builder func()) *customWidget {
	return &customWidget{
		builder: builder,
	}
}
