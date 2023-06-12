# Contributing to StrongDMM

We love your input! We want to make contributing to this project as easy and transparent as possible, whether it's:

- Reporting a bug
- Discussing the current state of the code
- Submitting a fix
- Proposing new features
- Becoming a maintainer

## We Develop with GitHub

We use GitHub to host code, to track issues and feature requests, as well as accept pull requests.

## We Use [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow), So All Code Changes Happen Through Pull Requests

Pull requests are the best way to propose changes to the codebase
(we use [GitHub Flow](https://docs.github.com/en/get-started/quickstart/github-flow)).

We actively welcome your pull requests:

1. Fork the repo and create your branch from `main`.
2. Make sure your code lints.
3. Issue that pulls request!

## Any contributions you make will be under the GNU General Public License v3.0

In short, when you submit code changes, your submissions are understood
to be under the same [GPL-3.0](https://choosealicense.com/licenses/gpl-3.0/) that covers the project.
Feel free to contact the maintainers if that's a concern.

## Report bugs using GitHub's [issues](https://github.com/SpaiR/StrongDMM/issues)
We use GitHub issues to track public bugs. Report a bug by [opening a new issue](); it's that easy!

## Write bug reports with detail, background, and sample code
**Great Bug Reports** tend to have:

- A quick summary and/or background
- Steps to reproduce
    - Be specific!
    - Give sample code if you can.
- What you expected would happen
- What actually happens
- Notes (possibly including why you think this might be happening, or stuff you tried that didn't work)

People *love* thorough bug reports. I'm not even kidding.

## Use a Consistent Coding Style

You can run [golangci-lint](https://golangci-lint.run/) to verify your changes beforehand.

## Use a Consistent Naming

Some things are named specifically. Please follow this guideline to make things consistent.

### Commit

Commit should be named in the next format:

```
tag: short description

Full scription.
```

For example:

```
fix: correct map parser output 

Fixes the problem when the map parser provides invalid output.
It happened because of the invalid arguments provided to the parse method.
```

When commit fixes/resolves/closes a specific GitHub issue, the description must contain proper note.
It should follow right after the short description and wrapped with empty lines:

```
fix: ...

fixes #0

...
```

#### Tags

- **ci:** everything connected with CI stuff;
- **doc:** all about the project documentation;
- **feature:** adding of massive functionality;
- **fix:** fixing and resolving bugs and problems;
- **refactor:** improves code without changing functionality;
- **tweak:** small improvements to the existing functionality, ex. wording changes, buttons movement etc;
- **up:** when bumping dependencies.

## License

By contributing, you agree that your contributions will be licensed under its GNU General Public License v3.0.
