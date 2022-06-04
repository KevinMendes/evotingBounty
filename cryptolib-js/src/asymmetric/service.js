/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const RsaPublicKey = require('./rsa-public-key');
const RsaPrivateKey = require('./rsa-private-key');
const KeyPair = require('./keypair');
const Signer = require('./signer');
const SignatureVerifier = require('./signature-verifier');
const validator = require('../input-validator');
const cryptoPolicy = require('../cryptopolicy');
const secureRandom = require('../securerandom');
const forge = require('node-forge');

module.exports = AsymmetricCryptographyService;

/**
 * @class AsymmetricCryptographyService
 * @classdesc The asymmetric cryptography service API. To instantiate this
 *            object, use the method {@link newService}.
 * @hideconstructor
 * @param {Object}
 *            [options] An object containing optional arguments.
 * @param {Policy}
 *            [options.policy=Default policy] The cryptographic policy to use.
 * @param {SecureRandomService}
 *            [options.secureRandomService=Created internally] The secure random
 *            service to use.
 */
function AsymmetricCryptographyService(options) {
	options = options || {};

	let _policy;
	if (options.policy) {
		_policy = options.policy;
	} else {
		_policy = cryptoPolicy.newInstance();
	}

	let _secureRandomService;
	if (options.secureRandomService) {
		_secureRandomService = options.secureRandomService;
	} else {
		_secureRandomService = secureRandom;
	}

	/**
	 * Creates a new RsaPublicKey object from a provided RSA public key or its
	 * components.
	 *
	 * @function newRsaPublicKey
	 * @memberof AsymmetricCryptographyService
	 * @param {Object}
	 *            params An object containing the input parameters for the
	 *            RsaPublicKey object creation.
	 * @param {string}
	 *            params.pem a PEM string representation of the key. Any
	 *            additional parameters will be ignored.
	 * @param {number}
	 *            params.n The modulus of the key. Required if
	 *            <code>params.pem</code> is undefined.
	 * @param {number}
	 *            params.e The public exponent of the key. Required if
	 *            <code>params.pem</code> is undefined.
	 * @returns {RsaPublicKey} The new RsaPublicKey object.
	 */
	this.newRsaPublicKey = function (params) {
		validator.checkPublicKeyParameters(
			params, 'Parameters object for creation of RsaPublicKey object');

		if (!params.pem) {
			return new RsaPublicKey(params);
		}

		let forgePublicKey;
		try {
			forgePublicKey = forge.pki.publicKeyFromPem(params.pem);
		} catch (error) {
			throw new Error(
				'Public key for creation of RsaPublicKey object could not be PEM decoded; ' +
				error);
		}

		return new RsaPublicKey({n: forgePublicKey.n, e: forgePublicKey.e});
	};

	/**
	 * Creates a new RsaPrivateKey object from a provided RSA private key or its
	 * components.
	 *
	 * @function newRsaPrivateKey
	 * @memberof AsymmetricCryptographyService
	 * @param {Object}
	 *            params An object containing the input parameters for the
	 *            RsaPrivateKey object creation.
	 * @param {Object}
	 *            params An object containing the required input parameters.
	 * @param {string}
	 *            params.pem a PEM string representation of the key. Any
	 *            additional parameters will be ignored.
	 * @param {number}
	 *            params.n The modulus of the key. Required if
	 *            <code>params.pem</code> is undefined.
	 * @param {number}
	 *            params.e The public exponent of the key. Required if
	 *            <code>params.pem</code> is undefined.
	 * @param {number}
	 *            params.d The private exponent of the key. Required if
	 *            <code>params.pem</code> is undefined.
	 * @param {number}
	 *            params.p The first prime of the key. Required if
	 *            <code>params.pem</code> is undefined.
	 * @param {number}
	 *            params.q The second prime of the key Required if
	 *            <code>params.pem</code> is undefined.
	 * @param {number}
	 *            params.dP The first exponent of the key. Required if
	 *            <code>params.pem</code> is undefined.
	 * @param {number}
	 *            params.dQ The second exponent of the key. Required if
	 *            <code>params.pem</code> is undefined.
	 * @param {number}
	 *            params.qInv The coefficient of the key. Required if
	 *            <code>params.pem</code> is undefined.
	 * @returns {RsaPrivateKey} The new RsaPrivateKey object.
	 */
	this.newRsaPrivateKey = function (params) {
		validator.checkPrivateKeyParameters(
			params, 'Parameters object for creation of RsaPrivateKey object');

		if (!params.pem) {
			return new RsaPrivateKey(params);
		}

		let forgePrivateKey;
		try {
			forgePrivateKey = forge.pki.privateKeyFromPem(params.pem);
		} catch (error) {
			throw new Error(
				'Private key for creation of RsaPrivateKey object could not be PEM decoded; ' +
				error);
		}

		return new RsaPrivateKey({
			n: forgePrivateKey.n,
			e: forgePrivateKey.e,
			d: forgePrivateKey.d,
			p: forgePrivateKey.p,
			q: forgePrivateKey.q,
			dP: forgePrivateKey.dP,
			dQ: forgePrivateKey.dQ,
			qInv: forgePrivateKey.qInv
		});
	};

	/**
	 * Creates a new KeyPair object from a provided key pair.
	 *
	 * @function newKeyPair
	 * @memberof AsymmetricCryptographyService
	 * @param {string}
	 *            publicKey The public key comprising the key pair, in PEM
	 *            format.
	 * @param {string}
	 *            privateKey The private key comprising the key pair, in PEM
	 *            format.
	 * @returns {KeyPair} The new KeyPair object.
	 */
	this.newKeyPair = function (publicKey, privateKey) {
		validator.checkIsNonEmptyString(publicKey, 'Public key, PEM encoded');
		validator.checkIsNonEmptyString(privateKey, 'Private key, PEM encoded');

		try {
			forge.pki.publicKeyFromPem(publicKey);
		} catch (error) {
			throw new Error(
				'Public key for creation of KeyPair object could not be PEM decoded; ' +
				error);
		}
		try {
			forge.pki.privateKeyFromPem(privateKey);
		} catch (error) {
			throw new Error(
				'Private key for creation of KeyPair object could not be PEM decoded; ' +
				error);
		}

		return new KeyPair(publicKey, privateKey);
	};

	/**
	 * Creates a new Signer object for digitally signing data. It must be
	 * initialized with a private key.
	 *
	 * @function newSigner
	 * @memberof AsymmetricCryptographyService
	 * @returns {Signer} The new Signer object.
	 */
	this.newSigner = function () {
		return new Signer(_policy, _secureRandomService);
	};

	/**
	 * Creates a new SignatureVerifier object for verifying digital
	 * signatures. It must be initialized with a public key.
	 *
	 * @function newSignatureVerifier
	 * @memberof AsymmetricCryptographyService
	 * @returns {SignatureVerifier} The new SignatureVerifier object.
	 */
	this.newSignatureVerifier = function () {
		return new SignatureVerifier(_policy);
	};
}
