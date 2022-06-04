/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* jshint maxlen: 666  */
const certificateService = require('cryptolib-js/src/certificate').newService();
const codec = require('cryptolib-js/src/codec');
const prng = require('cryptolib-js/src/securerandom')
	.newService()
	.newRandomGenerator();
const asymmetricService = require('cryptolib-js/src/asymmetric').newService();
const signer = asymmetricService.newSigner();
const verifier = asymmetricService.newSignatureVerifier();

module.exports = (function () {
	'use strict';

	// validate server challenge
	// returns: client challenge if ok

	const validateServerChallenge = function (responseData, data, credentials) {
		// verify response

		const serverChallengeMessage = responseData.serverChallengeMessage;
		if (
			!serverChallengeMessage.serverChallenge ||
			!serverChallengeMessage.timestamp ||
			!serverChallengeMessage.signature
		) {
			throw new Error('Bad server challenge');
		}

		// verify server challenge signature
		const certificates = responseData.certificates;
		const publicKey = certificateService.newX509Certificate(
			certificates.authenticationTokenSignerCert,
		).publicKey;
		const dataToVerify =
			data.challengeValue +
			data.timestamp +
			data.electionEventId +
			data.credentialId;
		let verified = false;
		try {
			if (
				verifier
					.init(publicKey)
					.verify(
						codec.base64Decode(serverChallengeMessage.signature),
						dataToVerify,
					)
			) {
				verified = true;
			}
		} catch (e) {
			// ignore
		}
		if (!verified) {
			throw new Error('Challenge verification has failed');
		}

		// generate client challenge
		const clientChallenge = codec.base64Encode(
			prng.nextBigIntegerByDigits(16).toString(),
		);

		// sign client challenge
		const dataToSign = serverChallengeMessage.signature + clientChallenge;
		const privateKey = credentials.authPrivateKey;
		let clientChallengeSignature = null;

		// on ms edge this sig fails randomly for reasons yet
		// unknown. as a workaround we verify it and retry a couple of times if
		// it fails

		const MAX_RETRIES = 3;
		let retries = 0;
		let clientVerified = false;
		const session = require('../client/session.js');
		const clientPubKey = certificateService.newX509Certificate(
			session('credentials').certificateAuth,
		).publicKey;
		do {
			try {
				clientChallengeSignature = signer.init(privateKey).sign(dataToSign);
				clientVerified = verifier
					.init(clientPubKey)
					.verify(clientChallengeSignature, dataToSign);
			} catch (e) {
				// ignore
			}
		} while (!clientVerified && ++retries < MAX_RETRIES);

		return {
			clientChallenge: clientChallenge,
			signature: codec.base64Encode(clientChallengeSignature),
		};
	};
	return validateServerChallenge;
})();
