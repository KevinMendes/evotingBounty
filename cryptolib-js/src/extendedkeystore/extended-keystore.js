/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const Pkcs12KeyStore = require('./pkcs12-keystore');
const cryptoPolicy = require('../cryptopolicy');
const elGamal = require('../elgamal');
const symmetric = require('../symmetric');
const pbkdf = require('../pbkdf');
const constants = require('./constants');
const validator = require('../input-validator');
const codec = require('../codec');
const forge = require('node-forge');

module.exports = ExtendedKeyStore;

const BigInteger = forge.jsbn.BigInteger;
const FROM_EXTENDED = 'from Extended key store;';

/**
 * @class ExtendedKeyStore
 * @classdesc Encapsulates an extended key store. To instantiate this object, use
 *            the method {@link KeyStoreService.newExtendedKeyStore}.
 * @hideconstructor
 * @param {Object|string}
 *            keyStore The provided extended key store, as an object with expected
 *            properties <b>OR</b> its JSON string representation.
 * @param {string}
 *            password The password to load the Extended key store.
 * @param {Object}
 *            [options] An object containing optional arguments.
 * @param {Policy}
 *            [options.policy=Default policy] The cryptographic policy to use.
 * @param {PbkdfRandomService}
 *            [options.pbkdfRandomService=Created internally] The PBKDF service
 *            to use.
 * @param {SymmetricCryptographyService}
 *            [options.symmetricCryptographyService=Created internally] The
 *            symmetric cryptography service to use.
 * @param {ElGamalCryptographyService}
 *            [options.elGamalCryptographyService=Created internally] The
 *            ElGamal cryptography service to use.
 * @throws {Error}
 *             If the input data validation fails or the underlying key store
 *             could not be loaded.
 */
function ExtendedKeyStore(keyStore, password, options) {
	let extendedKeyStore = keyStore;
	if (typeof keyStore === 'string') {
        validator.checkIsJsonString(
            keyStore, 'Extended key store (in JSON format) to load');
        extendedKeyStore = JSON.parse(keyStore);
    }
    checkExtendedKeyStore(extendedKeyStore);

    validator.checkIsType(password, 'string', 'Password to load Extended key store');

    options = options || {};

	let _policy;
	if (options.policy) {
        _policy = options.policy;
    } else {
        _policy = cryptoPolicy.newInstance();
    }

	let pbkdfService;
	if (options.pbkdfService) {
        pbkdfService = options.pbkdfService;
    } else {
        pbkdfService = pbkdf.newService({policy: _policy});
    }

	let _symmetricService;
	if (options.symmetricCryptographyService) {
        _symmetricService = options.symmetricCryptographyService;
    } else {
        _symmetricService = symmetric.newService({policy: _policy});
    }

	let _elGamalService;
	if (options.elGamalCryptographyService) {
        _elGamalService = options.elGamalCryptographyService;
    } else {
        _elGamalService = elGamal.newService();
    }

	const _pbkdfDeriver = pbkdfService.newDeriver();
	const _symmetricCipher = _symmetricService.newCipher();

	const _derivedKeys = {};
	const _derivedPasswords = {};

	let _pkcs12KeyStore;
	try {
        _pkcs12KeyStore =
            new Pkcs12KeyStore(extendedKeyStore.store, getDerivedPassword(password));
    } catch (error) {
        throw new Error(`Could not load underlying PKCS12 key store; ${error}`);
    }

    /**
     * Retrieves a private key stored inside the Extended key store, given the
     * key's storage alias name and password.
     *
     * @function getPrivateKey
     * @memberof ExtendedKeyStore
     * @param {string}
     *            alias The storage alias name of the private key to retrieve.
     * @param {string}
     *            password The storage password of the private key.
     * @returns {string} The private key, in PEM format.
     * @throws {Error}
     *             If the input data validation fails or the private key could
     *             not be retrieved.
     */
    this.getPrivateKey = function (alias, password) {
        validator.checkIsNonEmptyString(
            alias,
            'Storage alias name of private key to retrieve from Extended key store');
        validator.checkIsType(
            password, 'string',
            'Password of private key to retrieve from Extended key store');

        try {
            return _pkcs12KeyStore.getPrivateKey(alias, getDerivedPassword(password));
        } catch (error) {
            throw new Error(`Could not retrieve private key with alias '${alias}' ${FROM_EXTENDED} ${error}`);
        }
    };

    /**
     * Retrieves a certificate stored inside the Extended key store, given the
     * storage alias name of the certificate or that of its associated private
     * key entry.
     *
     * @function getCertificate
     * @memberof ExtendedKeyStore
     * @param {string}
     *            alias The storage alias name of the certificate or that of its
     *            associated private key entry.
     * @returns {string} The certificate, in PEM format.
     * @throws {Error}
     *             If the input data validation fails or the certificate could
     *             not be retrieved.
     */
    this.getCertificate = function (alias) {
        validator.checkIsNonEmptyString(
            alias,
            'Storage alias name of certificate to retrieve from Extended key store,');

        try {
            return _pkcs12KeyStore.getCertificate(alias);
        } catch (error) {
			throw new Error(`Could not retrieve certificate with alias '${alias}' ${FROM_EXTENDED} ${error}`);
        }
    };

    /**
     * Retrieves a certificate stored inside the Extended key store, given the
     * certificate's subject common name.
     *
     * @function getCertificateBySubject
     * @memberof ExtendedKeyStore
     * @param {string}
     *            subjectCn The subject common name of the certificate.
     * @returns {string} The certificate, in PEM format.
     * @throws {Error}
     *             If the input data validation fails or the certificate could
     *             not be retrieved.
     */
    this.getCertificateBySubject = function (subjectCn) {
        validator.checkIsNonEmptyString(
            subjectCn,
            'Subject common name of certificate to retrieve from Extended key store');

        try {
            return _pkcs12KeyStore.getCertificateBySubject(subjectCn);
        } catch (error) {
			throw new Error(`Could not retrieve certificate with subject common name '${subjectCn}' ${FROM_EXTENDED} ${error}`);
        }
    };

    /**
     * Retrieves a certificate chain stored inside the Extended key store, given
     * the storage alias name of the chain's associated private key entry.
     *
     * @function getCertificateChain
     * @memberof ExtendedKeyStore
     * @param {string}
     *            alias The storage alias name of the chain's associated private
     *            key entry.
     * @returns {string[]} The certificate chain, as an array of strings in PEM
     *          format.
     * @throws {Error}
     *             If the input data validation fails or the certificate chain
     *             could not be retrieved.
     */
    this.getCertificateChain = function (alias) {
        validator.checkIsNonEmptyString(
            alias,
            'Storage alias name of certificate chain to retrieve from Extended key store');

        try {
            return _pkcs12KeyStore.getCertificateChain(alias);
        } catch (error) {
			throw new Error(`Could not retrieve certificate chain with alias '${alias}' ${FROM_EXTENDED} ${error}`);
        }
    };

    /**
     * Retrieves a secret key stored inside the Extended key store, given the key's
     * storage alias name and password.
     *
     * @function getSecretKey
     * @memberof ExtendedKeyStore
     * @param {string}
     *            alias The storage alias name of the secret key to retrieve.
     * @param {string}
     *            password The storage password of the secret key.
     * @returns {Uint8Array} The secret key.
     * @throws {Error}
     *             If the input data validation fails or the secret key could
     *             not be retrieved.
     */
    this.getSecretKey = function (alias, password) {
        validator.checkIsNonEmptyString(
            alias,
            'Storage alias name of secret key to retrieve from Extended key store');
        validator.checkIsType(
            password, 'string',
            'Password of secret key to retrieve from key store');

		const encryptedKey = extendedKeyStore.secrets[alias];
		checkEncryptedKey(encryptedKey, 'secret', alias);

		const secretKey = getKey(password, encryptedKey, alias, 'secret');

		return codec.binaryDecode(secretKey);
    };

    /**
     * Retrieves an ElGamal private key stored inside the Extended key store, given
     * the key's storage alias name and password.
     *
     * @function getElGamalPrivateKey
     * @memberof ExtendedKeyStore
     * @param {string}
     *            alias The storage alias name of the ElGamal private key to
     *            retrieve.
     * @param {string}
     *            password The storage password of the ElGamal private key.
     * @returns {ElGamalPrivateKey} The ElGamal private key.
     * @throws {Error}
     *             If the input data validation fails or the ElGamal private key
     *             could not be retrieved.
     */
    this.getElGamalPrivateKey = function (alias, password) {
        validator.checkIsNonEmptyString(
            alias,
            'Storage alias name of ElGamal private key to retrieve from Extended key store');
        validator.checkIsType(
            password, 'string',
            'Password of ElGamal private key to retrieve from Extended key store');

		const encryptedKey = extendedKeyStore.egPrivKeys[alias];
		checkEncryptedKey(encryptedKey, 'ElGamal private', alias);

		const elGamalPrivateKeyJson =
			getKey(password, encryptedKey, alias, 'ElGamal private');

		return _elGamalService.newPrivateKey(elGamalPrivateKeyJson);
    };

    function getDerivedPassword(password) {
        if (!Object.prototype.hasOwnProperty.call(_derivedPasswords, password)) {
			const derivedKeyBytes = getDerivedKey(password);
			const derivedKeyBigInteger = new BigInteger(derivedKeyBytes);
			const derivedPassword =
				derivedKeyBigInteger.toString(constants.CHARACTER_MAX_RADIX);

			_derivedPasswords[password] = derivedPassword;
        }

        return _derivedPasswords[password];
    }

    function getDerivedKey(password) {
        if (!Object.prototype.hasOwnProperty.call(_derivedKeys, password)) {
            _derivedKeys[password] = _pbkdfDeriver.derive(
                password, codec.base64Decode(extendedKeyStore.salt));
        }

        return _derivedKeys[password];
    }

    function checkEncryptedKey(key, keyType, alias) {
        if (!key) {
			throw new Error(`Could not find ${keyType} key with storage alias name '${alias}'`);
        }
    }

    function getKey(password, encryptedKey, alias, keyType) {
		const aliasAndKey = _symmetricCipher.init(getDerivedKey(password))
			.decrypt(codec.base64Decode(encryptedKey));

		const aliasAndKeyBinaryEncoded = codec.binaryEncode(aliasAndKey);

		const decryptedAlias = aliasAndKeyBinaryEncoded.slice(0, alias.length);

		if (decryptedAlias !== alias) {
			throw new Error(`Expected decrypted alias for ${keyType} key to be '${alias}'; Found '${decryptedAlias}'\
			 Check password provided to retrieve key from store.`);
        }

        return aliasAndKeyBinaryEncoded.slice(alias.length, aliasAndKey.length);
    }

    function checkExtendedKeyStore(extendedKeyStore) {
        validator.checkIsObjectWithProperties(
            extendedKeyStore, 'Extended key store to load');

        if (typeof extendedKeyStore.salt === 'undefined') {
            throw new Error('Field \'salt\' is undefined in Extended key store to load');
        }

        if (typeof extendedKeyStore.store === 'undefined') {
            throw new Error(
                'Field \'store\' is undefined in Extended key store to load');
        }
    }
}
