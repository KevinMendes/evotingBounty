# Javascript Voting Client (voting-client-js)

The voting-client-js builds upon the crypto-primitives-ts and cryptolib-js librairies and defines a javascript frontend API for the voter-portal. The crypto-primitives-ts and cryptolib-js librairies implement the underlying cryptographic primitives.
The voting-client-js provides the following functionalities:

- Authenticating the voter based upon the Start Voting Key and, possibly, an additional authentication factor (date of birth, year of birth).
- Encrypting the vote
- Calculating the partial Choice Return Codes and encrypting them
- Generating zero-knowledge proofs linking the encrypted vote and the partial Choice Return Codes
- Signing the vote
- Generating the confirmation key

The voting-client-js provides a javascript API, which can be invoked by the front-end voter-portal.

## Usage

The build process generates an obfuscated and minimized bundle file called `ov-api.min.js`, which can be used directly, or published to a repository. After publication, this package can be accessed as an NPM dependency via the package name `ov-api`.
&nbsp;

## Build instructions

See also the build instructions in the readme of the repository 'evoting' for compiling the voting-client-js module.

### Prerequisites

Make sure that the following third party software is installed:

#### Windows

- `Node.js`
- `Mozilla Firefox`
- `Google Chrome`

#### Linux (Operating system)

- `Node.js`
- `Mozilla Firefox`
- `Google Chrome`

#### Linux (Windows Subsystem for Linux (WSL))

- `Node.js`

**NOTE:** When using WSL, you will have to set the environment variable `CHROME_BIN` to the path of your Windows Google Chrome executable file.
For a standard Chrome installation, this can be achieved by adding the following line to your shell startup file `$HOME/.profile`:

```text
export CHROME_BIN="/mnt/c/Program Files (x86)/Google/Chrome/Application/chrome.exe"
```

### How to install dependencies

Change to the top level directory of this package and install its dependencies as:

```text
npm install
```

### How to build package and run tests

From the top level directory of this package, build it and the run the tests as:

```text
npm run build
```

### Browsers for tests

The tests can be run, using one of the following three browsers:

- `FirefoxHeadless`
- `ChromeHeadless`
- `ChromeHeadlessNoSandbox`

The browser used for the tests can be changed in the following three files, which reside in the top level directory of this package,

- `karma.conf.js`
- `karma.proofs.conf.js`

by replacing the default browser with the browser of your choice in the following line:

```text
    browsers: ['<default browser>'],
```

**NOTE:** When using WSL, always set this browser to `ChromeHeadlessNoSandbox` in all three karma files mentioned above.
