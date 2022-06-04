/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const Pkcs12KeyStore = require('./pkcs12-keystore');
const ExtendedKeyStore = require('./extended-keystore');

module.exports = KeyStoreService;

/**
 * @class KeyStoreService
 * @classdesc The key store service API. To instantiate this object, use the
 *            method {@link newService}.
 * @hideconstructor
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
 */
function KeyStoreService(options) {
    /**
     * Creates a new Pkcs12KeyStore object and loads it with the provided PKCS
     * #12 key store.
     *
     * @function newPkcs12KeyStore
     * @memberof KeyStoreService
     * @param {Uint8Array|string}
     *            keyStore The provided PKCS #12 key store, as a DER encoded
     *            ASN.1 structure <b>OR</b> such a structure Base64 encoded.
     * @param {string}
     *            password The password to load the PKCS #12 key store.
     * @returns {Pkcs12KeyStore} The Pkcs12KeyStore object.
     * @throws {Error}
     *             If the input data validation fails or the PKCS #12 key store
     *             could not be loaded.
     */
    this.newPkcs12KeyStore = function (keyStore, password) {
        return new Pkcs12KeyStore(keyStore, password);
    };

    /**
     * Creates a new ExtendedKeyStore object and loads it with the provided extended
     * key store.
     *
     * @function newExtendedKeyStore
     * @memberof KeyStoreService
     * @param {Object|string}
     *            keyStore The provided Extended key store, as an object with
     *            expected properties <b>OR</b> its JSON string representation.
     * @param {string}
     *            password The password to load the Extended key store.
     * @returns {ExtendedKeyStore} The ExtendedKeyStore object.
     * @throws {Error}
     *             If the input data validation fails or the Extended key store
     *             could not be loaded.
     */
    this.newExtendedKeyStore = function (keyStore, password) {
        return new ExtendedKeyStore(keyStore, password, options);
    };
}
