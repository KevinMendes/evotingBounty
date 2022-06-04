/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* jshint maxlen: 666  */
const certificateService = require('cryptolib-js/src/certificate').newService();
const asymmetricService = require('cryptolib-js/src/asymmetric').newService();
const codec = require('cryptolib-js/src/codec');

module.exports = (function () {
	'use strict';

	const session = require('../client/session.js');
	const jsrsasign = require('jsrsasign');
	const _ = require('lodash');

	const verifyJWSignature = function (jwSignature, publicKeyCertificate) {
		const publicKey = jsrsasign.KEYUTIL.getKey(publicKeyCertificate);
		return jsrsasign.jws.JWS.verifyJWT(jwSignature, publicKey, {
			alg: ['PS256'],
		});
	};

	const verifyBallotSignature = function (ballot, publicKeyCertificate) {
		return verifyJWSignature(ballot.signature, publicKeyCertificate);
	};

	const verifyBallotTextsSignature = function (ballotTextsSignature, publicKeyCertificate) {
		return verifyJWSignature(ballotTextsSignature, publicKeyCertificate);
	};

	const validateAuthToken = function (authToken, authenticationTokenSignerCert) {
		// verify authtoken structure

		const tokenSignature = codec.base64Decode(authToken.signature);
		const voterInformation = authToken.voterInformation;

		if (
			!authToken.id ||
			!voterInformation.votingCardId ||
			!voterInformation.ballotId ||
			!voterInformation.credentialId ||
			!voterInformation.ballotBoxId ||
			!voterInformation.verificationCardId ||
			!voterInformation.verificationCardSetId ||
			!voterInformation.votingCardSetId
		) {
			throw new Error('TOKEN_ERROR');
		}

		// verify server auth token signature

		const dataToVerify =
			authToken.id +
			authToken.timestamp +
			voterInformation.tenantId +
			voterInformation.electionEventId +
			voterInformation.votingCardId +
			voterInformation.ballotId +
			voterInformation.credentialId +
			voterInformation.verificationCardId +
			voterInformation.ballotBoxId +
			voterInformation.verificationCardSetId +
			voterInformation.votingCardSetId;

		const publicKey = certificateService.newX509Certificate(
			authenticationTokenSignerCert,
		).publicKey;
		try {
			if (
				!asymmetricService
					.newSignatureVerifier()
					.init(publicKey)
					.verify(tokenSignature, dataToVerify)
			) {
				throw new Error('TOKEN_ERROR');
			}
		} catch (e) {
			// ignore details
			throw new Error('TOKEN_ERROR');
		}
	};

	const validateAuthTokenResponse = function (
		response,
		certificates,
		credentialId,
		electionEventId,
	) {
		const authToken = response.authenticationToken;
		const voterInformation = authToken.voterInformation;

		// verify authentication structure and signatures

		validateAuthToken(authToken, certificates['authenticationTokenSignerCert']);

		const adminBoardCertificate = session('certificates')['adminBoard'];

		if (!verifyBallotSignature(response.ballot, adminBoardCertificate)) {
			throw new Error('TOKEN_ERROR');
		}

		_.each(response.ballotTextsSignature, function (ballotTextSignature) {
			if (
				!verifyBallotTextsSignature(
					ballotTextSignature.signedObject,
					adminBoardCertificate,
				)
			) {
				throw new Error('TOKEN_ERROR');
			}
		});

		// parse ballot as it comes in string format
		const ballot = JSON.parse(jsrsasign.jws.JWS.parse(response.ballot.signature).payloadObj.objectToSign);

		if (
			voterInformation.credentialId !== credentialId ||
			voterInformation.electionEventId !== electionEventId ||
			voterInformation.ballotId !== ballot.id ||
			voterInformation.ballotBoxId !== response.ballotBox.id ||
			voterInformation.verificationCardId !== response.verificationCard.id
		) {
			throw new Error('TOKEN_ERROR');
		}
	};

	return {
		validateAuthTokenResponse: validateAuthTokenResponse,
		validateAuthToken: validateAuthToken,
	};
})();
