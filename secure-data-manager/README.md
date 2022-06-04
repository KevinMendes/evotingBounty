# Secure Data Manager

The Secure Data Manager implements the setup component's and offline control component's algorithms of the Swiss Post Voting System. In the configuration phase, the setup component combines the control components' contributions and generates the codes. In the tally phase, the offline mixing control component
(CCM) performs the final mixing and decryption of the votes.

The protocol requires **different** instances of the Secure Data Manager:

- The Secure Data Manager for the configuration phase (Setup component functionality)
- The Secure Data Manager for the tally phase (Offline CCM functionality)
- The online Secure Data Manager for transferring data between the Secure Data Manager and the
  voting server/control components

The Secure Data Manager's execution must fulfill the following conditions.

- The Secure Data Manager is operated by the cantons, **not** by Swiss Post.
- The setup component and offline CCM Secure Data Manager instances are **offline**. They transfer data only via secure USB to the online Secure Data Manager.
- The machines running the Secure Data Manager are hardened and have no other purpose than running the
  Secure Data Manager software.
- The cantonal administrator stores the Secure Data Manager machines securely during the voting phase.

We assume that the setup component Secure Data Manager and the offline CCM Secure Data Manager
do *not* share confidential data and that the information they write in their internal file system remains secret.

During the configuration phase, the Secure Data Manager provides the following functionalities.

- Implements the GenVerCardSetKeys algorithm
- Implements the GenSetupEncryptionKeys algorithm
- Implements the GenVerDat algorithm
- Implements the CombineEncLongCodesShares algorithm
- Implements the GenCMTable algorithm
- Implements the GenCredDat algorithm
- Implements the SetupTallyEB algorithm
- Splits the electoral board secret key and administration board key and distributes it to the
  board members.
- Signs the generated data with the administration board secret key.

In the tally phase, the Secure Data Manager provides the following functionalities.

- Re-constitutes the electoral board secret key
- Implements the MixDecOffline algorithm
- Implements the DecodePlaintexts algorithm

## Usage

The Secure Data Manager has a SpringBoot backend and an AngularJS frontend, deployed on NW.js (previously known as node-webkit). The frontend interacts with the Secure Data Manager backends via HTTP calls. Spring-Batch is used for batch processing during the configuration phase.

In general, the Secure Data Manager heeds web application security best practices when appropriate. However, we do not enforce authentication between the application's frontend and backend parts, and we omit HTTP security headers. Please note that while the Secure Data Manager uses web technologies for the user interface, the Secure Data Manager Backend accepts only local traffic. If the adversary controls the Secure Data Manager instance, he could access the internal file system, and sniffing the local HTTP traffic would be pointless. To prevent an attacker from controlling a Secure Data Manager instance, we implement the operational safeguards described above.

## Development

Check the build instructions in the readme of the repository 'evoting' 

For development purposes, you can launch the Secure Data Manager Frontend with the following options:

* _--expireAB_ {minutes} : Expiration in minutes of the administration board.
* _--expireEA_ {minutes} : Expiration in minutes of the electoral board
* _--frontend-only_ : Avoid the automatic launch of the local backend service.

## Run

Certain operations run significantly faster using native optimizations. You can check the [crypto-primitives readme](https://gitlab.com/swisspost-evoting/crypto-primitives/crypto-primitives) for configuring native library support.

## Attribution

- This product includes software developed by IAIK of Graz University of Technology
  - [https://jce.iaik.tugraz.at/products/core-crypto-toolkits/pkcs11-wrapper/](https://jce.iaik.tugraz.at/products/core-crypto-toolkits/pkcs11-wrapper/)
- The Secure Data Manager Frontend uses icons and ressources from Google's Material Design (Apache 2.0)
  - [https://github.com/google/material-design-icons](https://github.com/google/material-design-icons)
  - [https://github.com/google/material-design-lite](https://github.com/google/material-design-lite)
