#[cfg(windows)]
extern crate winres;

use std::process::Command;

fn main() {
    add_git_hash();
    add_exe_icon();
}

fn add_git_hash() {
    let output = Command::new("git")
        .args(&["rev-parse", "--verify", "--short", "HEAD"])
        .output()
        .unwrap();
    let git_hash = String::from_utf8(output.stdout).unwrap();
    println!("cargo:rustc-env=GIT_HASH={}", git_hash);
}

#[cfg(target_os = "linux")]
fn add_exe_icon() {
    // do nothing
}

#[cfg(target_os = "windows")]
fn add_exe_icon() {
    let mut res = winres::WindowsResource::new();
    res.set_icon("res/icon.ico");
    res.compile().unwrap();
}
