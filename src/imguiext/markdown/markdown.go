package markdown

import (
	"bufio"
	"github.com/SpaiR/imgui-go"
	"sdmm/app/window"
	"strings"
)

type Block int

const (
	Text Block = iota
	H1
	H2
	H3
	List
	Line
)

type Entry struct {
	Block  Block
	String string
}

type Markdown struct {
	Entries []Entry
}

var (
	blocks = map[string]Block{
		"# ":   H1,
		"## ":  H2,
		"### ": H3,
		" * ":  List,
		"---":  Line,
	}
)

func Parse(text string) (markdown Markdown) {
	scanner := bufio.NewScanner(strings.NewReader(text))
	for scanner.Scan() {
		line := scanner.Text()

		var parsed bool
		for prefix, block := range blocks {
			if strings.HasPrefix(line, prefix) {
				parsed = true
				markdown.Entries = append(markdown.Entries, Entry{
					Block:  block,
					String: strings.TrimPrefix(line, prefix),
				})
				break
			}
		}

		if !parsed {
			markdown.Entries = append(markdown.Entries, Entry{
				Block:  Text,
				String: line,
			})
		}
	}
	return markdown
}

func Show(markdown Markdown) {
	for _, entry := range markdown.Entries {
		switch entry.Block {
		case Text:
			ShowText(entry.String)
		case H1:
			ShowHeader(entry.String, window.FontH1)
		case H2:
			ShowHeader(entry.String, window.FontH2)
		case H3:
			ShowHeader(entry.String, window.FontH3)
		case List:
			ShowList(entry.String)
		case Line:
			ShowLine()
		}
	}
}

func ShowText(text string) {
	imgui.TextWrapped(text)
}

func ShowHeader(text string, font imgui.Font) {
	imgui.PushFont(font)
	imgui.TextWrapped(text)
	imgui.PopFont()
}

func ShowList(text string) {
	imgui.Bullet()
	imgui.SameLine()
	imgui.TextWrapped(text)
}

func ShowLine() {
	imgui.Separator()
}
