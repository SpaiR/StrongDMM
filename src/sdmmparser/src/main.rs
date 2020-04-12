extern crate dreammaker as dm;
extern crate serde;
#[macro_use]
extern crate serde_derive;
extern crate serde_json;

use std::env;
use std::fs;
use std::str;

use dm::constants::Constant;
use dm::objtree::TypeRef;
use dm::Context;

#[derive(Serialize)]
pub struct ObjectTreeType {
    pub path: String,
    pub vars: Vec<ObjectTreeVar>,
    pub children: Vec<ObjectTreeType>,
}

#[derive(Serialize)]
pub struct ObjectTreeVar {
    pub name: String,
    pub value: String,
}

fn main() {
    let args: Vec<String> = env::args().collect();

    let exit_code = if args.len() < 2 {
        println!("Invalid arguments count, expected: 2");
        1
    } else {
        parse_env(&args[1], &args[2])
    };

    std::process::exit(exit_code);
}

fn parse_env(env_path: &str, file_name: &str) -> i32 {
    let objtree = match Context::default().parse_environment(env_path.as_ref()) {
        Ok(t) => t,
        Err(_e) => return 1,
    };

    let root = recurse_objtree(objtree.root());
    let json = serde_json::to_string(&root).unwrap();

    return match fs::write(file_name, json) {
        Ok(_t) => 0,
        Err(_e) => 1,
    };
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
