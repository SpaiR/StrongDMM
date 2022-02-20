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

func (m Markdown) IsEmpty() bool {
	return len(m.Entries) == 0
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

const (
	markerNewLine = "<br>"
)

func Parse(text string) (markdown Markdown) {
	var entries []Entry

	{
		var idx int
		scanner := bufio.NewScanner(strings.NewReader(text))
		for scanner.Scan() {
			line := scanner.Text()

			var parsed bool
			for prefix, block := range blocks {
				if strings.HasPrefix(line, prefix) {
					parsed = true
					entries = append(entries, Entry{
						Block:  block,
						String: strings.TrimPrefix(line, prefix),
					})
					break
				}
			}

			// Text block processing.
			if !parsed {
				txt := strings.Trim(line, " \n")

				// Concat previous text block with the current one.
				// Result will look pretty much the same as a standard markdown.
				if len(txt) > 0 && idx != 0 && entries[idx-1].Block == Text && len(entries[idx-1].String) > 0 {
					nexText := entries[idx-1].String

					if !strings.HasSuffix(nexText, "\n") {
						nexText += " "
					}

					entries[idx-1].String = nexText + strings.ReplaceAll(txt, markerNewLine, "\n")
					continue // Skip the current block if its content was merged to the previous one.
				}

				entries = append(entries, Entry{
					Block:  Text,
					String: strings.ReplaceAll(txt, markerNewLine, "\n"),
				})
			}

			idx++
		}
	}

	for _, entry := range entries {
		markdown.Entries = append(markdown.Entries, entry)
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
			ShowLine()
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
