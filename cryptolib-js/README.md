# Javascript cryptographic library

*cryptolib-js* is the reference cryptographic library and implements the voting client's cryptographic primitives such as ElGamal encryption and zero-knowledge proofs.
The voting-client-js builds upon the cryptolib-js. The voter-portal frontend relies on the voting-client-js to implement the protocol's algorithms.

## Usage

The library is structured as one script containing all the cryptographic components.
To use any of these components, they can be required by referencing the main entry of each sub lib:

```javascript
const asymmetric = require('cryptolib-js/src/asymmetric');
```

## Development

Each *cryptolib-js* component is set up as a sub-folder in the `src/` folder.

Various npm phases are available to help development:

```bash
npm run clean
npm run compile
npm install
npm run test
npm run coverage:report
npm run test:coverage
```

### Clean

To clean _node_modules_, _coverage_ and _package-lock.json_ before install.

```sh
npm run clean
```

### Code validation

[ESLint](https://eslint.org/) takes care of ensuring the code follows the basic conventions and helps preventing common pitfalls.

```sh
npm run compile
```

### Install dependencies

To install all node modules dependencies defined in _package.json_.

```sh
npm install
```

### Tests

To run all the tests found in the _./test_ folder.

```sh
npm run test
```

### Coverage

To generate a _lcov_ coverage report to be use by [SonarQube](https://www.sonarqube.org/). Should be executed after running all the tests. 

```sh
npm run coverage:report
```

### Tests & Coverage

To run all tests and generate a coverage report.

```sh
npm run test:coverage
```


## Maven lifecycle phases

The *cryptolib-js* is available through Maven reactor build.
It uses the [exec-maven-plugin](https://www.mojohaus.org/exec-maven-plugin/) to map NPM phases to maven ones. 

| Maven phase | NPM phase | Description |
| ------------| --------- | ----------- |
| clean | clean | Deletes all generated/installed files, such as _node_modules_, _coverage_ report and _package-lock.json_ |
| generate-sources | install | Downloads and installs all dependencies described in package.json |
| compile | compile | Executes a static analysis of the code with [ESLint](https://eslint.org/) tool |
| test | test:coverage | Runs a bunch of tests and provides a coverage report |

### Skip tests

To skip the tests run, you can use _skipTests_ option:

```sh
mvn install -DskipTests
```