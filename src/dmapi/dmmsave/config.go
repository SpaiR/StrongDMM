package dmmsave

type Format uint

const (
	FormatInitial Format = iota
	FormatTGM
	FormatDM
)

type Config struct {
	Format Format
}
