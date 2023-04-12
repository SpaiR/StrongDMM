use std::panic;

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
    decl: bool,
    is_tmp: bool,
    is_const: bool,
    is_static: bool,
}

pub fn parse_environment(path: String) -> String {
    match panic::catch_unwind(|| {
        match parse(&path) {
            Some(json) => json,
            None => format!("error: unable to parse environment {}", path)
        }
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
        vars: Vec::with_capacity(ty.vars.len()),
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
            decl: var.declaration.is_some(),
            is_tmp: var.declaration.as_ref().map_or(false, |d| d.var_type.flags.is_tmp()),
            is_const: var.declaration.as_ref().map_or(false, |d| d.var_type.flags.is_const()),
            is_static: var.declaration.as_ref().map_or(false, |d| d.var_type.flags.is_static()),
        });
    }

    for child in ty.children() {
        entry.children.push(recurse_objtree(child));
    }

    entry
}
