use dm::constants::Constant;
use dm::Context;
use dm::objtree::TypeRef;

#[derive(Serialize)]
struct ObjectTreeType {
    path: String,
    vars: Vec<ObjectTreeVar>,
    children: Vec<ObjectTreeType>,
}

#[derive(Serialize)]
struct ObjectTreeVar {
    name: String,
    value: String,
}

pub fn parse_environment(path: String) -> String {
    match parse(&path) {
        Some(json) => json,
        None => format!("Unable to parse environment {}", path)
    }
}

fn parse(env_path: &str) -> Option<String> {
    let objtree = match Context::default().parse_environment(env_path.as_ref()) {
        Ok(t) => t,
        Err(_e) => return None,
    };

    let root = recurse_objtree(objtree.root());
    let json = serde_json::to_string(&root).unwrap();

    return Some(json);
}

fn recurse_objtree(ty: TypeRef) -> ObjectTreeType {
    let mut entry = ObjectTreeType {
        path: ty.path.to_owned(),
        vars: Vec::new(),
        children: Vec::new(),
    };

    for (name, var) in ty.vars.iter() {
        entry.vars.push(ObjectTreeVar {
            name: name.to_owned(),
            value: var
                .value
                .constant
                .as_ref()
                .unwrap_or(Constant::null())
                .to_string(),
        });
    }

    for child in ty.children() {
        entry.children.push(recurse_objtree(child));
    }

    entry
}
