extern crate dirs;
extern crate indicatif;
extern crate reqwest;
extern crate zip;

use std::env;
use std::fs;
use std::fs::{File, OpenOptions};
use std::io::{Read, Write};
use std::path::PathBuf;
use std::process::Command;

mod archive;
mod download;

const EDITOR_HOME_DIR_NAME: &str = ".strongdmm";
const LOCAL_VERSION_FILE_NAME: &str = "version";
const REMOTE_VERSION_PATH: &str = "https://spair.github.io/StrongDMM/version.txt";
const DOWNLOAD_PATH: &str = "https://github.com/SpaiR/StrongDMM/releases/download";

#[cfg(target_os = "linux")]
const DOWNLOAD_FILE_NAME: &str = "strongdmm-linux.zip";

#[cfg(target_os = "windows")]
const DOWNLOAD_FILE_NAME: &str = "strongdmm-windows.zip";

fn main() {
    print_logo();

    println!("Checking for updates...");

    let editor_home_dir_path = get_editor_home_dir_path();
    let editor_bin_dir_path = get_editor_editor_bin_dir_path(&editor_home_dir_path);

    let mut local_version_file = get_local_version_file(&editor_home_dir_path);
    let local_version = read_local_version(&mut local_version_file);
    let remote_version = read_remote_version();

    println!(" * Local version: {}", local_version);
    println!(" * Remote version: {}", remote_version);

    let zip_file = if local_version != remote_version {
        println!("Downloading...");
        remove_bin_dir(&editor_bin_dir_path);
        Some(download_remote_version(&remote_version))
    } else {
        println!("Local version is up to date!");
        None
    };

    if let Some(zip_file) = zip_file {
        println!("Installing downloaded version...");

        archive::unzip(zip_file, &editor_bin_dir_path)
            .expect("downloaded archive should installed");

        update_local_version(&mut local_version_file, &remote_version);
    }

    println!("Starting the editor...");
    start_the_editor(&editor_bin_dir_path);
}

fn print_logo() {
    println!(include_str!("../res/logo.txt"));
    println!(
        ":: StrongDMM Launcher ::  (v{})  [{}]\n",
        env!("CARGO_PKG_VERSION"),
        env!("GIT_HASH")
    );
}

fn get_editor_home_dir_path() -> PathBuf {
    let user_home_dir_path = dirs::home_dir().expect("user home dir should be available");

    let editor_home_dir_path = get_child_path(&user_home_dir_path, EDITOR_HOME_DIR_NAME);

    if !editor_home_dir_path.exists() {
        fs::create_dir(&editor_home_dir_path)
            .expect("editor home dir should be available for creation");
    }

    editor_home_dir_path
}

fn get_editor_editor_bin_dir_path(editor_home_dir_path: &PathBuf) -> PathBuf {
    let editor_bin_dir_path = get_child_path(&editor_home_dir_path, "bin");

    if !editor_bin_dir_path.exists() {
        fs::create_dir(&editor_bin_dir_path)
            .expect("editor bin dir should be available for creation");
    }

    editor_bin_dir_path
}

fn get_local_version_file(editor_home_dir_path: &PathBuf) -> File {
    let version_file_path = get_child_path(editor_home_dir_path, LOCAL_VERSION_FILE_NAME);

    OpenOptions::new()
        .read(true)
        .write(true)
        .create(true)
        .open(version_file_path)
        .expect("local version file should be read or created")
}

fn read_local_version(local_version_file: &mut File) -> String {
    let mut local_version = String::new();

    local_version_file
        .read_to_string(&mut local_version)
        .expect("local version file should be available for read");

    local_version = local_version.trim().to_string();

    if local_version.is_empty() {
        local_version.push_str("unknown");
    }

    local_version
}

fn read_remote_version() -> String {
    let resp = reqwest::blocking::get(REMOTE_VERSION_PATH);
    resp.unwrap_or_else(|_| {
        drop_with_error(
            format!(
                "unable to get remote application version by path: {}",
                REMOTE_VERSION_PATH
            )
            .as_str(),
        )
    })
    .text()
    .expect("remote editor version should be available")
    .trim()
    .to_string()
}

fn remove_bin_dir(editor_bin_dir_path: &PathBuf) {
    fs::remove_dir_all(&editor_bin_dir_path)
        .expect("editor binaries folder should be available for deletion");
}

fn download_remote_version(remote_version: &str) -> File {
    download::download_with_pb(
        format!(
            "{}/{}/{}",
            DOWNLOAD_PATH, remote_version, DOWNLOAD_FILE_NAME
        )
        .as_str(),
    )
    .unwrap_or_else(|msg| drop_with_error(msg.as_str()))
}

fn update_local_version(local_version_file: &mut File, new_version: &str) {
    local_version_file.write(new_version.as_bytes()).unwrap();
}

#[cfg(target_os = "linux")]
fn start_the_editor(editor_bin_dir_path: &PathBuf) {
    Command::new("sh")
        .current_dir(get_child_path(&editor_bin_dir_path, "strongdmm"))
        .arg("run.sh")
        .spawn()
        .expect("failed to start the editor");
}

#[cfg(target_os = "windows")]
fn start_the_editor(editor_bin_dir_path: &PathBuf) {
    Command::new("cmd")
        .current_dir(get_child_path(&editor_bin_dir_path, "strongdmm"))
        .args(&["/C", "run.bat"])
        .spawn()
        .expect("failed to start the editor");
}

fn get_child_path(root: &PathBuf, child: &str) -> PathBuf {
    PathBuf::from(format!(
        "{}/{}",
        root.as_path().display().to_string(),
        child
    ))
}

fn drop_with_error(error: &str) -> ! {
    println!("Error: {}", error);
    std::process::exit(1)
}
