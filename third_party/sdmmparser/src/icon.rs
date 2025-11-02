use std::{fs, io::BufReader, panic};

use dm::dmi::*;

#[derive(Serialize)]
struct IconMetadata {
    width: u32,
    height: u32,
    states: Vec<IconState>,
}

#[derive(Serialize)]
struct IconState {
    name: String,
    dirs: u32,
    frames: u32,
}

pub fn parse_icon_metadata(path: String) -> String {
    match panic::catch_unwind(|| match parse(&path) {
        Some(json) => json,
        None => format!("error: unable to parse icon metadata {}", path),
    }) {
        Ok(result) => result,
        Err(e) => {
            if let Some(e) = e.downcast_ref::<String>() {
                format!("error: {}", e)
            } else {
                String::from("error: unknown")
            }
        }
    }
}

fn parse(path: &str) -> Option<String> {
    fs::File::open(path).map(BufReader::new).map_or(None, |f| {
        png::Decoder::new(f).read_info().map_or(None, |reader| {
            for text_chunk in &reader.info().compressed_latin1_text {
                if text_chunk.keyword.eq("Description") {
                    return text_chunk.get_text().map_or(None, |info| {
                        Metadata::meta_from_str(info.as_str())
                            .map_or(None, |metadata| Some(meta2json(metadata)))
                    });
                }
            }
            None
        })
    })
}

fn meta2json(metadata: Metadata) -> String {
    let mut states: Vec<IconState> = Vec::new();

    for state in metadata.states {
        states.push(IconState {
            name: state.name,
            dirs: match state.dirs {
                Dirs::One => 1,
                Dirs::Four => 4,
                Dirs::Eight => 8,
            },
            frames: state.frames.count() as u32,
        });
    }

    let icon_metadata = IconMetadata {
        width: metadata.width,
        height: metadata.height,
        states,
    };

    return serde_json::to_string(&icon_metadata).unwrap();
}
