# Control Components
The control components generate the return codes, shuffle the encrypted votes, and decrypt them at the end of the election. We assume that there are four control components and at least one (per type) is trustworthy. There are two types of control component modules:

| Name     | Description    | 
| --------|---------|
| **Return Codes Control Component**  | Compute the Choice Return Codes and the Vote Cast Return Code—in interaction with the setup component (configuration phase) and the voting server (voting phase).    |
| **Mixing Control Component** | Mix and partially decrypt ciphertexts in the ballot box.  |

## Usage
The control components modules are packaged in a single standalone SpringBoot applications.

## Development

```
mvn clean install
```

## Run

Certain operations run significantly faster using native optimizations. You can check the [crypto-primitives readme](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives) for configuring native library support. 
