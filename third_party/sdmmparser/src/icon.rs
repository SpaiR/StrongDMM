use std::path::Path;

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
    match parse(&path) {
        Some(json) => json,
        None => format!("Unable to parse icon metadata {}", path)
    }
}

fn parse(path: &str) -> Option<String> {
    match Metadata::from_file(Path::new(path)) {
        Ok((_, meta)) => {
            Some(meta2json(meta))
        }
        Err(_) => None
    }
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
            frames: state.frames.len() as u32,
        });
    }

    let icon_metadata = IconMetadata {
        width: metadata.width,
        height: metadata.height,
        states,
    };

    return serde_json::to_string(&icon_metadata).unwrap();
}
