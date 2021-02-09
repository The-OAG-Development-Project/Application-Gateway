# Contributing

🎉 First of all, thanks much for contributing to the OWASP Application Gateway! All kind of help, contribution, and support is much appreciated and will make it a little bit easier to build secure web applications!

## How to contribute

Nellgateway is still at a very early stage. Therefore you have a wide variety of options on how to contribute to the project:

- Implement new features or fix open bugs: Please check out the GitHub issues; we also mark issues as beginner-friendly if you are new to the project.
- Propose new features: If you have an idea for a useful feature for OWAG, feel free to open a GitHub issue and explain the feature and why you think it would be helpful.  If you have any inputs to the security and software architecture of OWAG, please also add a new GitHub issue.
- Test it: If you used the gateway in your own project, any feedback would be much appreciated.
- Other: If you have any other idea on how to contribute: Just do it :)

If you are interested in the project but still have any questions, please feel free to contact the core contributors directly.

## Branching and Versioning

We use a bit simplified version of the git-flow for our branching (https://nvie.com/posts/a-successful-git-branching-model/). We currently have the following two long-lived branches:

- `main`: The main branch is used for releases. We tag each release with its version number. (i.e. "v0.3")
- `dev`: The dev branch should always build, but we are a bit more reluctant here. Usually, we merge all features to this branch until we release a new version. Pre-releases are tagged on this branch.

This means if you want to implement a feature, please fork the repository, add your feature and create a pull request back to the dev branch. If you are a regular contributor, we can also add you as a collaborator to the repository.

We use semantic version numbers (MAJOR.MINOR.PATCH) except that we didn't yet use patch numbers. (See https://semver.org) We are currently still in the development stage, so we don't have a stable public API yet, and anything can change at any time.

## Idea setup

### Manual compilation

```bash
# Clone the repo
git clone https://github.com/gianlucafrei/Application-Gateway.git
cd Application-Gateway

# Build manually
cd nellygateway #Inner source folder 
./mvnw -B package #You can of course also use your local maven installation instead of the wrapper
```

### IntelliJ

If you use IntelliJ you just import the inner source folder (`...repo/nellygateway/`) and everything should work out of the box.

Please use the following code style file:

It is also recommended to use the "Save Actions" plugin to automatically optimize imports and formatting when you safe the file.

## CI/CD

We currently use the following tools for CI/CD

- GitHub Actions for automatic build and releases (See https://github.com/gianlucafrei/nellygateway/actions)
- Codacy for static analysis (See https://app.codacy.com/gh/gianlucafrei/nellygateway/dashboard)
- Dockle for container security analysis (Within build pipelines: https://github.com/gianlucafrei/nellygateway/blob/main/.github/workflows/main.yml, https://github.com/goodwithtech/dockle)


## Varia

💡 If you have any inputs on how to improve this contributing guildeline or think we should do things differently feal free to contact us or create a pull request for this file.
