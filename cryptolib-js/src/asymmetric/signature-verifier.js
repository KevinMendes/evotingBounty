/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const Policy = require('../cryptopolicy');
const validator = require('../input-validator');
const codec = require('../codec');
const forge = require('node-forge');

module.exports = SignatureVerifier;

const NOT_SUPPORTED = 'is not supported.';

/**
 * @class SignatureVerifier
 * @classdesc The digital signature verifier API. To instantiate this object,
 *            use the method
 *            {@link AsymmetricCryptographyService.newSignatureVerifier}.
 * @hideconstructor
 * @param {Policy}
 *            policy The cryptographic policy to use.
 */
function SignatureVerifier(policy) {
    // PRIVATE ///////////////////////////////////////////////////////////////////

    policy.asymmetric.signer = policy.asymmetric.signer || {};

	let _publicExponent;
	let _digester;
	let _padding;
	let _forgePublicKey;
	let _updated;

	function initForgeSignatureVerifier() {
        if (policy.asymmetric.signer.publicExponent ===
            Policy.options.asymmetric.signer.publicExponent.F4) {
            _publicExponent = policy.asymmetric.signer.publicExponent;
        } else {
            throw new Error(`Signature verifier public exponent '${policy.asymmetric.signer.publicExponent}' ${NOT_SUPPORTED}`);
        }

        if (policy.asymmetric.signer.algorithm !==
            Policy.options.asymmetric.signer.algorithm.RSA) {
            throw new Error(`Signature verifier algorithm '${policy.asymmetric.signer.algorithm}' ${NOT_SUPPORTED}`);
        }

        if (policy.asymmetric.signer.hashAlgorithm ===
            Policy.options.asymmetric.signer.hashAlgorithm.SHA256) {
            _digester = forge.md.sha256.create();
        } else if (
            policy.asymmetric.signer.hashAlgorithm ===
            Policy.options.asymmetric.signer.hashAlgorithm.SHA512_224) {
            _digester = forge.md.sha512.sha224.create();
        } else {
            throw new Error(`Signature verifier hash algorithm '${policy.asymmetric.signer.hashAlgorithm}' ${NOT_SUPPORTED}`);
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
                    throw new Error(`Signature verifier PSS padding hash algorithm\
                     '${policy.asymmetric.signer.padding.hashAlgorithm}' ${NOT_SUPPORTED}`);
                }

                policy.asymmetric.signer.padding.maskGenerator =
                    policy.asymmetric.signer.padding.maskGenerator || {};
				const paddingMgf = createMgf(policy);

				_padding = forge.pss.create({
                    md: paddingMd,
                    mgf: paddingMgf,
                    saltLength: policy.asymmetric.signer.padding.saltLengthBytes
                });
            } else {
                throw new Error(`Signature verifier padding '${policy.asymmetric.signer.padding.name} ' ${NOT_SUPPORTED}`);
            }
        }

        _updated = false;
    }

    // CONSTRUCTOR ///////////////////////////////////////////////////////////////

    initForgeSignatureVerifier();

    // PUBLIC ////////////////////////////////////////////////////////////////////

    /**
     * Initializes the signature verifier with the provided public key.
     *
     * @function init
     * @memberof SignatureVerifier
     * @param {string}
     *            publicKey The public key with which to initialize the
     *            signature verifier, in PEM format.
     * @returns {SignatureVerifier} A reference to this object, to facilitate
     *          method chaining.
     * @throws {Error}
     *             If the input data validation fails.
     */
    this.init = function (publicKey) {
        validator.checkIsNonEmptyString(publicKey,'Public key (PEM encoded) with which to initialize signature verifier');

        try {
            _forgePublicKey = forge.pki.publicKeyFromPem(publicKey);
        } catch (error) {
            throw new Error(`Public key with which to initialize signature verifier could not be PEM decoded; ${error}`);
        }

		const publicExponentFound = _forgePublicKey.e.toString();
		const publicExponentExpected = _publicExponent.toString();
		if (publicExponentFound !== publicExponentExpected) {
            throw new Error(`Expected public key with which to initialize signature verifier to have same public exponent as cryptographic policy:\
             ${publicExponentExpected} ; Found ${publicExponentFound}`);
        }

        return this;
    };

    /**
     * Verifies the digital signature of the provided data. If there were any
     * prior calls to the method <code>update</code>, then the provided data
     * will be bitwise appended to the data provided to those calls. If no data
     * is provided here the signature will only be verified for the data
     * provided to prior calls to the method <code>update</code>. The
     * signature verifier will be automatically reinitialized after this method
     * completes. Before using this method, the signature verifier must have
     * been initialized with a public key, via the method
     * {@link SignatureVerifier.init}.
     *
     * @function verify
     * @memberof SignatureVerifier
     * @param {Uint8Array}
     *            signature The digital signature to verify.
     * @param {Uint8Array|string}
     *            [data] Some data that was digitally signed. <b>NOTE:</b> Data
     *            of type <code>string</code> will be UTF-8 encoded.
     * @returns {boolean} <code>true</code> if the signature was verified,
     *          <code>false</code> otherwise.
     * @throws {Error}
     *             If the input data validation fails, the signature verifier
     *             was not initialized, the signature verifier was not updated
     *             with any data or the signature verification process fails.
     */
    this.verify = function (signature, data) {
        try {
            if (typeof _forgePublicKey === 'undefined') {
                throw new Error('Digital signature verifier has not been initialized with any public key');
            }

            validator.checkIsInstanceOf(signature, Uint8Array, 'Uint8Array', 'Digital signature to verify');

            if (typeof data !== 'undefined') {
                if (typeof data === 'string') {
                    data = codec.utf8Encode(data);
                }
                validator.checkIsInstanceOf(
                    data, Uint8Array, 'Uint8Array',
                    'Data provided to signature verifier');
                this.update(data);
            } else if (!_updated) {
                throw new Error(
                    'Attempt to verify a signature without either providing data as input or having made a previous call to method \'update\'');
            }

			const verified = _forgePublicKey.verify(
				_digester.digest().getBytes(), codec.binaryEncode(signature),
				_padding);

			_digester.start();
            _updated = false;

            return verified;
        } catch (error) {
            throw new Error(`Digital signature could not be verified; ${error}`);
        }
    };

    /**
     * Updates the signature verifier with the provided data. The data will be
     * internally bitwise concatenated to any data provided during previous
     * calls to this method, after the last call to the method
     * <code>verify</code>. Before using this method, the signature verifier
     * must have been initialized with a public key, via the method
     * {@link SignatureVerifier.init}.
     *
     * @function update
     * @memberof SignatureVerifier
     * @param {Uint8Array|string}
     *            data The data with which to update the signature verifier.
     *            <b>NOTE:</b> Data of type <code>string</code> will be UTF-8
     *            encoded.
     * @returns {SignatureVerifier} A reference to this object, to facilitate
     *          method chaining.
     * @throws {Error}
     *             If the input data validation fails or the update process
     *             fails.
     */
    this.update = function (data) {
        try {
            if (typeof _forgePublicKey === 'undefined') {
                throw new Error('Digital signature verifier has not been initialized with any public key');
            }

            if (typeof data === 'string') {
                data = codec.utf8Encode(data);
            }
            validator.checkIsInstanceOf(
                data, Uint8Array, 'Uint8Array',
                'Data with which to update signature verifier');

            _digester.update(codec.binaryEncode(data));
            _updated = true;
        } catch (error) {
            throw new Error(`Digital signature verifier could not be updated; ${error}`);
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
            throw new Error(`Signature verifier PSS padding mask generation function hash algorithm\
             '${policy.asymmetric.signer.padding.maskGenerator.hashAlgorithm}' ${NOT_SUPPORTED}`);
        }
    } else {
        throw new Error(`Signature verifier PSS padding mask generation function\
         '${policy.asymmetric.signer.padding.maskGenerator.name}' ${NOT_SUPPORTED}`);
    }
}
