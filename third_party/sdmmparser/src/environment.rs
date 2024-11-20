use std::panic;

use dm::constants::Constant;
use dm::objtree::TypeRef;
use dm::Context;

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
    match panic::catch_unwind(|| match parse(&path) {
        Some(json) => json,
        None => format!("parser error: unable to parse environment {}", path),
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
    let ctx = Context::default();
    let objtree = ctx.parse_environment(env_path.as_ref()).ok()?;

    if let Some(m) = errors_message(&ctx) {
        return Some(m);
    }

    let root = recurse_objtree(objtree.root());
    serde_json::to_string(&root).ok()
}

fn errors_message(ctx: &Context) -> Option<String> {
    let ctx_e = ctx.errors();
    let errors: Vec<_> = ctx_e
        .iter()
        .filter(|e| e.severity() == dm::Severity::Error)
        .collect();

    if errors.is_empty() {
        return None;
    }

    let msg = errors
        .iter()
        .filter_map(|e| {
            ctx.file_path(e.location().file).to_str().map(|file_path| {
                format!(
                    "  {} - [{}:{}] | {}",
                    file_path,
                    e.location().line,
                    e.location().column,
                    e.description(),
                )
            })
        })
        .collect::<Vec<_>>()
        .join("\n");

    Some(format!("parser error: compilation errors\n{}", msg))
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
            is_tmp: var
                .declaration
                .as_ref()
                .map_or(false, |d| d.var_type.flags.is_tmp()),
            is_const: var
                .declaration
                .as_ref()
                .map_or(false, |d| d.var_type.flags.is_const()),
            is_static: var
                .declaration
                .as_ref()
                .map_or(false, |d| d.var_type.flags.is_static()),
        });
    }

    for child in ty.children() {
        entry.children.push(recurse_objtree(child));
    }

    entry
}
