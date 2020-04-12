use std::fs::File;
use std::path::PathBuf;
use std::{fs, io};
use zip::result::ZipError;

pub fn unzip(zip_file: File, dest_path: &PathBuf) -> Result<(), ZipError> {
    let mut archive = zip::ZipArchive::new(zip_file)?;

    for i in 0..archive.len() {
        let mut file = archive.by_index(i)?;
        let out_path = dest_path.join(file.sanitized_name());

        if (&*file.name()).ends_with('/') {
            fs::create_dir_all(&out_path)?;
        } else {
            if let Some(p) = out_path.parent() {
                if !p.exists() {
                    fs::create_dir_all(&p)?;
                }
            }

            let mut out_file = fs::File::create(&out_path)?;
            io::copy(&mut file, &mut out_file)?;
        }

        // Get and Set permissions
        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt;
            if let Some(mode) = file.unix_mode() {
                fs::set_permissions(&out_path, fs::Permissions::from_mode(mode))
                    .expect("file permissions should be set");
            }
        }
    }

    return Ok(());
}
