use std::fs::File;
use std::io::{self, copy, Read};

use indicatif::{ProgressBar, ProgressStyle};
use reqwest::header;
use reqwest::Url;

struct DownloadProgress<R> {
    inner: R,
    progress_bar: ProgressBar,
}

impl<R: Read> Read for DownloadProgress<R> {
    fn read(&mut self, buf: &mut [u8]) -> io::Result<usize> {
        self.inner.read(buf).map(|n| {
            self.progress_bar.inc(n as u64);
            n
        })
    }
}

pub fn download_with_pb(url: &str) -> Result<File, String> {
    let url = match Url::parse(url) {
        Ok(url) => url,
        Err(_) => return Err(format!("unable to parse url {}", url)),
    };

    let client = reqwest::blocking::Client::new();

    let total_size = {
        let resp = client.get(url.as_str()).send().unwrap();
        if resp.status().is_success() {
            resp.headers()
                .get(header::CONTENT_LENGTH)
                .and_then(|ct_len| ct_len.to_str().ok())
                .and_then(|ct_len| ct_len.parse().ok())
                .unwrap_or(0)
        } else {
            return Err("unable to parse file content length".to_string());
        }
    };

    let request = client.get(url.as_str());

    let pb = ProgressBar::new(total_size);
    pb.set_style(ProgressStyle::default_bar()
        .template("{spinner:.green} [{elapsed_precise}] [{bar:40.cyan/blue}] {bytes}/{total_bytes} ({eta})")
        .progress_chars("#>-"));

    let mut source = DownloadProgress {
        progress_bar: pb,
        inner: request.send().expect("download request should be sent"),
    };

    let mut dest = match tempfile::tempfile() {
        Ok(f) => f,
        Err(_) => return Err("unable to create tmp file".to_string()),
    };

    if copy(&mut source, &mut dest).is_err() {
        return Err("unable to read downloaded content".to_string());
    }

    Ok(dest)
}
