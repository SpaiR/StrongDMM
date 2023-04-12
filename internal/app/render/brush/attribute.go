package brush

type attribute struct {
	size       int32
	xtype      uint32
	xtypeSize  int32
	normalized bool
}

type attributesList struct {
	stride int32
	attrs  []attribute
}

func (a *attributesList) addAttribute(attribute attribute) {
	a.attrs = append(a.attrs, attribute)
	a.stride += attribute.xtypeSize * attribute.size
}
