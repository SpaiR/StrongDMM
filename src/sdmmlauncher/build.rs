use std::process::Command;

fn main() {
    add_git_hash()
}

fn add_git_hash() {
    let output = Command::new("git")
        .args(&["rev-parse", "--verify", "--short", "HEAD"])
        .output()
        .unwrap();
    let git_hash = String::from_utf8(output.stdout).unwrap();
    println!("cargo:rustc-env=GIT_HASH={}", git_hash);
}
