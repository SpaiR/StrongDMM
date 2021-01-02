extern crate core;
extern crate dreammaker as dm;
extern crate serde;
#[macro_use]
extern crate serde_derive;
extern crate serde_json;

use core::mem;
use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use std::str;

use environment::parse_environment;
use icon::parse_icon_metadata;

mod environment;
mod icon;

#[no_mangle]
#[allow(non_snake_case)]
pub extern fn SdmmParseEnvironment(native_path: *const c_char) -> *const c_char {
    to_ptr(parse_environment(to_string(native_path)))
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern fn SdmmParseIconMetadata(native_path: *const c_char) -> *const c_char {
    to_ptr(parse_icon_metadata(to_string(native_path)))
}

#[no_mangle]
#[allow(non_snake_case)]
pub extern fn SdmmFreeStr(native_str: *mut c_char) {
    unsafe { CString::from_raw(native_str) };
}

/// Convert a native string to a Rust string
fn to_string(pointer: *const c_char) -> String {
    let slice = unsafe { CStr::from_ptr(pointer).to_bytes() };
    str::from_utf8(slice).unwrap().to_string()
}

/// Convert a Rust string to a native string
fn to_ptr(string: String) -> *const c_char {
    let cs = CString::new(string.as_bytes()).unwrap();
    let ptr = cs.as_ptr();
    // Tell Rust not to clean up the string while we still have a pointer to it.
    // Otherwise, we'll get a segfault.
    mem::forget(cs);
    ptr
}
