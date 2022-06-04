/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const Policy = require('../cryptopolicy');
const validator = require('../input-validator');
const codec = require('../codec');
const forge = require('node-forge');

module.exports = Signer;

const NOT_SUPPORTED = 'is not supported.';

/**
 * @class Signer
 * @classdesc The digital signer API. To instantiate this object, use the method
 *            {@link AsymmetricCryptographyService.newSigner}.
 * @hideconstructor
 * @param {Policy}
 *            policy The cryptographic policy to use.
 * @param {SecureRandomService}
 *            secureRandomService The secure random service to use.
 */
function Signer(policy, secureRandomService) {
	// PRIVATE ///////////////////////////////////////////////////////////////////

	policy.asymmetric.signer = policy.asymmetric.signer || {};

	let _publicExponent;
	let _digester;
	let _padding;
	let _forgePrivateKey;
	let _updated;

	function initForgeSigner() {
		if (policy.asymmetric.signer.publicExponent ===
			Policy.options.asymmetric.signer.publicExponent.F4) {
			_publicExponent = policy.asymmetric.signer.publicExponent;
		} else {
			throw new Error(`Signer public exponent '${policy.asymmetric.signer.publicExponent}' ${NOT_SUPPORTED}`);
		}

		if (policy.asymmetric.signer.algorithm !==
			Policy.options.asymmetric.signer.algorithm.RSA) {
			throw new Error(`Signer algorithm '${policy.asymmetric.signer.algorithm}' ${NOT_SUPPORTED}`);
		}

		if (policy.asymmetric.signer.hashAlgorithm ===
			Policy.options.asymmetric.signer.hashAlgorithm.SHA256) {
			_digester = forge.md.sha256.create();
		} else if (
			policy.asymmetric.signer.hashAlgorithm ===
			Policy.options.asymmetric.signer.hashAlgorithm.SHA512_224) {
			_digester = forge.md.sha512.sha224.create();
		} else {
			throw new Error(`Signer hash algorithm '${policy.asymmetric.signer.hashAlgorithm}' ${NOT_SUPPORTED}`);
		}
		_digester.start();

		if (typeof policy.asymmetric.signer.padding !== 'undefined') {
			if (policy.asymmetric.signer.padding.name ===
				Policy.options.asymmetric.signer.padding.PSS.name) {
				let paddingMd;
				if (policy.asymmetric.signer.padding.hashAlgorithm ===
					Policy.options.asymmetric.signer.padding.PSS.hashAlgorithm.SHA256) {
					paddingMd = forge.md.sha256.create();
				} else if (
					policy.asymmetric.signer.padding.hashAlgorithm ===
					Policy.options.asymmetric.signer.padding.PSS.hashAlgorithm
						.SHA512_224) {
					paddingMd = forge.md.sha512.sha224.create();
				} else {
					throw new Error(`Signer PSS padding hash algorithm '${policy.asymmetric.signer.padding.hashAlgorithm}' ${NOT_SUPPORTED}`);
				}

				policy.asymmetric.signer.padding.maskGenerator =
					policy.asymmetric.signer.padding.maskGenerator || {};
				const paddingMgf = createMgf(policy);

				_padding = forge.pss.create({
					md: paddingMd,
					mgf: paddingMgf,
					saltLength: policy.asymmetric.signer.padding.saltLengthBytes,
					prng: secureRandomService.newRandomGenerator()
				});
			} else {
				throw new Error(`Signer padding '${policy.asymmetric.signer.padding.name}' ${NOT_SUPPORTED}`);
			}
		}

		_updated = false;
	}

	// CONSTRUCTOR ///////////////////////////////////////////////////////////////

	initForgeSigner();

	// PUBLIC ////////////////////////////////////////////////////////////////////

	/**
	 * Initializes the digital signer with the provided private key.
	 *
	 * @function init
	 * @memberof Signer
	 * @param {string}
	 *            privateKey The private key with which to initialize the
	 *            digital signer, in PEM format.
	 * @returns {Signer} A reference to this object, to facilitate method
	 *          chaining.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	this.init = function (privateKey) {
		validator.checkIsNonEmptyString(
			privateKey,
			'Private key (PEM encoded) with which to initialize digital signer');

		try {
			_forgePrivateKey = forge.pki.privateKeyFromPem(privateKey);
		} catch (error) {
			throw new Error(`Private key with which to initialize signer could not be PEM decoded; ${error}`);
		}

		const publicExponentFound = _forgePrivateKey.e.toString();
		const publicExponentExpected = _publicExponent.toString();
		if (publicExponentFound !== publicExponentExpected) {
			throw new Error(`Expected private key with which to initialize signer to have same public exponent as cryptographic policy:\
			 ${publicExponentExpected} ; Found ${publicExponentFound}`);
		}

		return this;
	};

	/**
	 * Digitally signs the provided data. If there were any prior calls to the
	 * method <code>update</code>, then the provided data will be bitwise
	 * appended to the data provided to those calls. If no data is provided here
	 * the signature will only be generated for the data provided to prior calls
	 * to the method <code>update</code>. The signer will be automatically
	 * reinitialized after this method completes. Before
	 * using this method, the signer must have been initialized with a private
	 * key, via the method {@link Signer.init}.
	 *
	 * @function sign
	 * @memberof Signer
	 * @param {Uint8Array|string}
	 *            [data] Some data to sign. <b>NOTE:</b> Data of type
	 *            <code>string</code> will be UTF-8 encoded.
	 * @returns {Uint8Array} The digital signature.
	 * @throws {Error}
	 *             If the input data validation fails, the signer was not
	 *             initialized, the signer was not updated with any data or the
	 *             digital signing process fails.
	 */
	this.sign = function (data) {
		try {
			if (typeof _forgePrivateKey === 'undefined') {
				throw new Error('Digital signer has not been initialized with any private key');
			}

			if (typeof data !== 'undefined') {
				if (typeof data === 'string') {
					data = codec.utf8Encode(data);
				}
				validator.checkIsInstanceOf(
					data, Uint8Array, 'Uint8Array', 'Data provided to digital signer');
				this.update(data);
			} else if (!_updated) {
				throw new Error(
					'Attempt to digitally sign without either providing data as input or having made a previous call to method \'update\'');
			}


			const signature = _forgePrivateKey.sign(_digester, _padding);

			_digester.start();
			_updated = false;

			return codec.binaryDecode(signature);
		} catch (error) {
			throw new Error(`Digital signature could not be generated; ${error}`);
		}
	};

	/**
	 * Updates the signer with the provided data. The data will be internally
	 * bitwise concatenated to any data provided during previous calls to this
	 * method, after the last call to the method <code>sign</code>. Before
	 * using this method, the signer must have been initialized with a private
	 * key, via the method {@link Signer.init}.
	 *
	 * @function update
	 * @memberof Signer
	 * @param {Uint8Array|string}
	 *            data The data with which to update the signer. <b>NOTE:</b>
	 *            Data of type <code>string</code> will be UTF-8 encoded.
	 * @returns {Signer} A reference to this object, to facilitate method
	 *          chaining.
	 * @throws {Error}
	 *             If the input data validation fails or the update process
	 *             fails.
	 */
	this.update = function (data) {
		try {
			if (typeof _forgePrivateKey === 'undefined') {
				throw new Error('Digital signer has not been initialized with any private key');
			}

			if (typeof data === 'string') {
				data = codec.utf8Encode(data);
			}
			validator.checkIsInstanceOf(
				data, Uint8Array, 'Uint8Array',
				'Data with which to update digital signer');

			_digester.update(codec.binaryEncode(data));
			_updated = true;
		} catch (error) {
			throw new Error(`Digital signer could not be updated; ${error}`);
		}

		return this;
	};
}

function createMgf(policy) {
	if (policy.asymmetric.signer.padding.maskGenerator.name ===
		Policy.options.asymmetric.signer.padding.PSS.maskGenerator.MGF1
			.name) {
		if (policy.asymmetric.signer.padding.maskGenerator.hashAlgorithm ===
			Policy.options.asymmetric.signer.padding.PSS.maskGenerator.MGF1
				.hashAlgorithm.SHA256) {
			return forge.mgf.mgf1.create(forge.md.sha256.create());
		} else if (
			policy.asymmetric.signer.padding.maskGenerator.hashAlgorithm ===
			Policy.options.asymmetric.signer.padding.PSS.maskGenerator.MGF1
				.hashAlgorithm.SHA512_224) {
			return forge.mgf.mgf1.create(forge.md.sha512.sha224.create());
		} else {
			throw new Error(`Signer PSS padding mask generation function hash algorithm\
			 '${policy.asymmetric.signer.padding.maskGenerator.hashAlgorithm}' ${NOT_SUPPORTED}`);
		}
	} else {
		throw new Error(`Signer PSS padding mask generation function '${policy.asymmetric.signer.padding.maskGenerator.name}' ${NOT_SUPPORTED}`);
	}
}
