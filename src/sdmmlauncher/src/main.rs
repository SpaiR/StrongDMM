extern crate colored;
extern crate core;
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

use colored::Colorize;

mod archive;
mod download;

const HOME_DIR_NAME: &str = ".strongdmm";
const LOCAL_VERSION_FILE_NAME: &str = "version";
const REMOTE_VERSION_PATH: &str = "https://spair.github.io/StrongDMM/version.txt";
const DOWNLOAD_PATH: &str = "https://github.com/SpaiR/test/releases/download";

#[cfg(target_os = "linux")]
const DOWNLOAD_FILE_NAME: &str = "strongdmm-linux.zip";

#[cfg(target_os = "windows")]
const DOWNLOAD_FILE_NAME: &str = "strongdmm-windows.zip";

fn main() {
    print_logo();

    println!("Checking for updates...");

    let home_dir_path = get_home_dir_path();
    let bin_dir_path = get_bin_dir_path(&home_dir_path);

    let mut local_version_file = get_local_version_file(&home_dir_path);
    let local_version = read_local_version(&mut local_version_file);
    let remote_version = read_remote_version();

    let zip_file = if local_version != remote_version {
        println!("Local version: {}", local_version);
        println!("Remote version: {}", remote_version);
        println!("{}", "Downloading...".yellow());
        remove_bin_dir(&bin_dir_path);
        Some(download_remote_version(&remote_version))
    } else {
        println!("{}", "Local version is up to date!".green());
        None
    };

    if let Some(zip_file) = zip_file {
        println!("Installing downloaded version...");
        archive::unzip(zip_file, &bin_dir_path).unwrap_or_else(|_| {
            drop_with_error("unable to install downloaded version");
        });
        update_local_version(&mut local_version_file, &remote_version);
    }

    println!("{}", "Starting the editor!".yellow().bold());

    if cfg!(target_os = "linux") {
        Command::new("sh")
            .current_dir(get_child_path(&bin_dir_path, "strongdmm"))
            .arg("run.sh")
            .spawn()
            .unwrap_or_else(|_| drop_with_error("unable to start the editor"));
    } else {
        Command::new("start")
            .current_dir(get_child_path(&bin_dir_path, "strongdmm"))
            .arg("\"\"")
            .arg("run.bat")
            .spawn()
            .unwrap_or_else(|_| drop_with_error("unable to start the editor"));
    }

    hide_console_window();
}

fn print_logo() {
    println!("{}", include_str!("../res/logo.txt").yellow());
    println!(
        "{}  {}  {}\n",
        ":: StrongDMM Launcher ::".green().bold(),
        "(v1.0.0)".magenta(),
        format!("[{}]", env!("GIT_HASH")).blue()
    );
}

fn get_home_dir_path() -> PathBuf {
    let gen_home_dir_path = dirs::home_dir().unwrap_or_else(|| {
        drop_with_error("unable to get a user home dir");
    });

    let home_dir_path = get_child_path(&gen_home_dir_path, HOME_DIR_NAME);

    if !home_dir_path.exists() {
        fs::create_dir(&home_dir_path)
            .unwrap_or_else(|_| drop_with_error("unable to create an application home dir"));
    }

    home_dir_path
}

fn get_bin_dir_path(home_dir_path: &PathBuf) -> PathBuf {
    let bin_dir_path = get_child_path(&home_dir_path, "bin");

    if !bin_dir_path.exists() {
        fs::create_dir(&bin_dir_path).unwrap_or_else(|_| {
            drop_with_error("unable to create a dir to store application binaries")
        });
    }

    bin_dir_path
}

fn get_local_version_file(home_dir_path: &PathBuf) -> File {
    let version_file_path = get_child_path(home_dir_path, LOCAL_VERSION_FILE_NAME);

    OpenOptions::new()
        .read(true)
        .write(true)
        .create(true)
        .open(version_file_path)
        .unwrap_or_else(|_| drop_with_error("unable to get local version file"))
}

fn read_local_version(local_version_file: &mut File) -> String {
    let mut local_version = String::new();

    local_version_file
        .read_to_string(&mut local_version)
        .unwrap_or_else(|_| drop_with_error("unable to read local application version"));

    local_version = local_version.trim().to_string();

    if local_version.is_empty() {
        local_version.push_str("unknown");
    }

    local_version
}

fn read_remote_version() -> String {
    let resp = reqwest::blocking::get(REMOTE_VERSION_PATH);
    resp.unwrap_or_else(|_| drop_with_error("unable to get remote application version"))
        .text()
        .unwrap_or_else(|_| drop_with_error("unable to parse remote application version"))
        .trim()
        .to_string()
}

fn remove_bin_dir(bin_dir_path: &PathBuf) {
    fs::remove_dir_all(&bin_dir_path)
        .unwrap_or_else(|_| drop_with_error("unable to remove dir with application binaries"));
}

fn download_remote_version(remote_version: &str) -> File {
    download::download_with_pb(
        format!(
            "{}/v{}/{}",
            DOWNLOAD_PATH, remote_version, DOWNLOAD_FILE_NAME
        )
        .as_str(),
    )
    .unwrap_or_else(|msg| drop_with_error(msg.as_str()))
}

fn update_local_version(local_version_file: &mut File, new_version: &str) {
    local_version_file.write(new_version.as_bytes()).unwrap();
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

fn hide_console_window() {
    #[cfg(target_os = "windows")]
    {
        extern crate winapi;
        extern crate user32;
        extern crate kernel32;

        use core::ptr;

        let window = unsafe { kernel32::GetConsoleWindow() };
        // https://msdn.microsoft.com/en-us/library/windows/desktop/ms633548%28v=vs.85%29.aspx
        if window != ptr::null_mut() {
            unsafe {
                user32::ShowWindow(window, winapi::SW_HIDE);
            }
        }
    }
}
