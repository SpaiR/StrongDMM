package component

// Component is an abstract struct to provide basic components behaviour.
// Components themselves are a singleton layout elements which have a persisted location between app startups.
type Component struct {
	visible bool
	focused bool

	onVisible []func(bool)
	onFocused []func(bool)
}

func (Component) PreProcess() {
	// do nothing
}

func (Component) Process(int32) {
	// do nothing
}

func (Component) PostProcess() {
	// do nothing
}

func (c Component) Visible() bool {
	return c.visible
}

func (c *Component) SetVisible(visible bool) {
	if c.visible != visible {
		c.visible = visible
		for _, listener := range c.onVisible {
			listener(visible)
		}
	}
}

func (c *Component) AddOnVisible(listener func(bool)) {
	c.onVisible = append(c.onVisible, listener)
}

func (c Component) Focused() bool {
	return c.focused
}

func (c *Component) SetFocused(focused bool) {
	if c.focused != focused {
		c.focused = focused
		for _, listener := range c.onFocused {
			listener(focused)
		}
	}
}

func (c *Component) AddOnFocused(listener func(bool)) {
	c.onFocused = append(c.onFocused, listener)
}
