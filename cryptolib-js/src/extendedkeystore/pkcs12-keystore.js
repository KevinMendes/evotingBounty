/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const validator = require('../input-validator');
const codec = require('../codec');
const forge = require('node-forge');

module.exports = Pkcs12KeyStore;

const IN_PKCS12 = 'in PKCS12 key store.';
const FROM_PKCS12 = 'from PKCS12 key store;';

/**
 * @class Pkcs12KeyStore
 * @classdesc Encapsulates a PKCS #12 key store. To instantiate this object, use
 *            the method {@link KeyStoreService.newPkcs12KeyStore}.
 * @hideconstructor
 * @param {Uint8Array|string}
 *            keyStore The provided PKCS #12 key store as a DER encoded ASN.1
 *            structure <b>OR</b> such a structure Base64 encoded.
 * @param {string}
 *            password The password to load the PKCS #12 key store.
 * @throws {Error}
 *             If the input data validation fails or the underlying key store
 *             could not be loaded.
 */
function Pkcs12KeyStore(keyStore, password) {
	let pkcs12Der;
	if (typeof keyStore !== 'string') {
        pkcs12Der = keyStore;
    } else {
        validator.checkIsNonEmptyString(
            keyStore, 'PKCS12 key store, Base64 encoded, to load');
        pkcs12Der = codec.base64Decode(keyStore);
    }
    validator.checkIsInstanceOf(
        pkcs12Der, Uint8Array, 'Uint8Array',
        'PKCS12 key store, DER encoded, to load');

    validator.checkIsType(
        password, 'string', 'Password to load PKCS12 key store');

	let _forgePkcs12Asn1;
	let _forgePkcs12;
	try {
        _forgePkcs12Asn1 = forge.asn1.fromDer(codec.binaryEncode(pkcs12Der), false);
        _forgePkcs12 =
            forge.pkcs12.pkcs12FromAsn1(_forgePkcs12Asn1, false, password);
    } catch (error) {
        throw new Error(`Could not load PKCS12 key store; ${error}`);
    }

    /**
     * Retrieves a private key stored inside the PKCS #12 key store, given the
     * key's storage alias name and password
     *
     * @function getPrivateKey
     * @memberof Pkcs12KeyStore
     * @param {string}
     *            alias The storage alias name of the private key to retrieve.
     * @param {string}
     *            password The password used to store the private key.
     * @returns {string} The private key, in PEM format.
     * @throws {Error}
     *             If the input data validation fails or the private key could
     *             not be retrieved.
     */
    this.getPrivateKey = function (alias, password) {
        validator.checkIsNonEmptyString(
            alias,
            'Storage alias name of private key to retrieve from PKCS12 key store');
        checkPassword(
            password, _forgePkcs12Asn1,
            'Password to retrieve private key from PKCS12 key store');

        try {
			let privateKeyPem;
			let privateKeyFound = false;
			for (let i = 0; i < _forgePkcs12.safeContents.length && !privateKeyFound; i++) {
				const safeContents = _forgePkcs12.safeContents[i];
				const result = getPrivateKeyInSafeBag(safeContents, alias);
				privateKeyFound = result.found;
                privateKeyPem = result.privateKeyPem;
            }

            if (!privateKeyPem) {
				throw new Error(`Could not find any private key with alias '${alias}' ${IN_PKCS12}`);
            }

            return privateKeyPem;
        } catch (error) {
			throw new Error(`Could not retrieve private key with alias '${alias}' ${FROM_PKCS12} ${error}`);
        }
    };

    /**
     * Retrieves a certificate stored inside the PKCS #12 key store, given the
     * storage alias name of the certificate or that of its associated private
     * key entry.
     *
     * @function getCertificate
     * @memberof Pkcs12KeyStore
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
            'Storage alias name of certificate or that of its associated private key entry, to retrieve from PKCS12 key store');

        try {
			let certificatePem;
			let certificateFound = false;
			for (let i = 0; i < _forgePkcs12.safeContents.length; i++) {
				const safeContents = _forgePkcs12.safeContents[i];
				const result = getCertificateInSafeBag(safeContents, alias);
				certificatePem = result.certificatePem;
                certificateFound = result.found;

                if (certificateFound) {
                    break;
                }
            }

            if (!certificatePem) {
				throw new Error(`Could not find any certificate with alias '${alias}' ${IN_PKCS12}`);
            }

            return certificatePem;
        } catch (error) {
			throw new Error(`Could not retrieve certificate with alias '${alias}' ${FROM_PKCS12} ${error}`);
        }
    };

    /**
     * Retrieves a certificate stored inside the PKCS #12 key store, given the
     * certificate's subject common name.
     *
     * @function getCertificateBySubject
     * @memberof Pkcs12KeyStore
     * @param {string}
     *            subjectCn The subject common name of the certificate.
     * @returns {string} The certificate, in PEM format.
     * @throws {Error}
     *             If the input data validation fails or the certificate could
     *             not be retrieved.
     */
    this.getCertificateBySubject = function (subjectCN) {
        validator.checkIsNonEmptyString(
            subjectCN,
            'Subject common name of certificate to retrieve from PKCS12 key store');

        try {
			let certificatePem;
			let certificateFound = false;
			for (let i = 0; i < _forgePkcs12.safeContents.length; i++) {
				const safeContents = _forgePkcs12.safeContents[i];
				const result = getCertificateSubjectInSafeBag(safeContents, subjectCN);
				certificatePem = result.certificatePem;
                certificateFound = result.found;

                if (certificateFound) {
                    break;
                }
            }

            if (!certificatePem) {
				throw new Error(`Could not find any certificate with subject common name '${subjectCN}' ${IN_PKCS12}`);
            }

            return certificatePem;
        } catch (error) {
			throw Error(`Could not retrieve certificate with subject common name '${subjectCN}' ${FROM_PKCS12} ${error}`);
        }
    };

    /**
     * Retrieves a certificate chain stored inside the PKCS #12 key store, given
     * the storage alias name of the chain's associated private key entry.
     *
     * @function getCertificateChain
     * @memberof Pkcs12KeyStore
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
            'Storage alias name of private key associated with certificate chain to retrieve from PKCS12 key store');

        try {
            // Loop through certificate safe bags of PKCS12.
			const certificatePemChain = [];
			const savedBags = {};
			let nextBagId;
			for (let i = 0; i < _forgePkcs12.safeContents.length; i++) {
				const safeContents = _forgePkcs12.safeContents[i];
				for (let j = 0; j < safeContents.safeBags.length; j++) {
					const safeBag = safeContents.safeBags[j];
					if (safeBag.type === forge.pki.oids.certBag) {
						let nextBag;

						// If no bags have been found yet and this is the safe bag
                        // associated with the alias (i.e. the leaf safe bag).
                        // Or if this happens to be the next bag in the chain.
                        if (isSafeBagWithAlias(nextBagId, safeBag, alias)) {
                            nextBag = safeBag;
                        }
                            // If this is a bag higher up in the chain, and so will need to be
                        // added later.
                        else {
                            savedBags[safeBag.cert.subject.hash] = safeBag;
                        }

                        // Add this bag and any saved bags to the chain, and in the proper
                        // order.
                        while (nextBag) {
							const certificatePem = forge.pki.certificateToPem(nextBag.cert);
							certificatePemChain.push(certificatePem);

                            // If this bag is the root certificate, then the process is
                            // finished.
                            if (nextBag.cert.subject.hash === nextBag.cert.issuer.hash) {
                                return certificatePemChain;
                            }

                            // Save the issuer of this bag as the ID of the next bag to add to
                            // the chain.
                            nextBagId = nextBag.cert.issuer.hash;

                            // Add the saved bag corresponding to the ID of the next bag to
                            // the chain.
                            nextBag = savedBags[nextBagId];
                        }
                    }
                }
            }

            // Check whether any certificate safe bags were found.
            checkFoundCertificateSafeBags(certificatePemChain, alias);

            return certificatePemChain;
        } catch (error) {
			throw new Error(`Could not retrieve certificate chain with storage alias name '${alias}' ${FROM_PKCS12} ${error}`);
        }
    };

    function checkPassword(password, pkcs12Asn1, label) {
        validator.checkIsType(password, 'string', label);

        try {
            forge.pkcs12.pkcs12FromAsn1(pkcs12Asn1, false, password);
        } catch (error) {
            throw new Error(label + ' is not valid');
        }
    }
}

function getPrivateKeyInSafeBag(safeContents, alias) {
    for (let j = 0; j < safeContents.safeBags.length; j++) {
		const safeBag = safeContents.safeBags[j];
		if (safeBag.type === forge.pki.oids.pkcs8ShroudedKeyBag &&
            safeBag.attributes.friendlyName[0] === alias) {
            return {
                privateKeyPem: forge.pki.privateKeyToPem(safeBag.key),
                found: true
            };
        }
    }

    return {privateKeyPem: null, found: false};
}

function getCertificateInSafeBag(safeContents, alias) {
    for (let j = 0; j < safeContents.safeBags.length; j++) {
		const safeBag = safeContents.safeBags[j];
		if (safeBag.type === forge.pki.oids.certBag && safeBag.attributes &&
            safeBag.attributes.friendlyName &&
            safeBag.attributes.friendlyName[0] === alias) {
            return {
                certificatePem: forge.pki.certificateToPem(safeBag.cert),
                found: true
            };
        }
    }

    return {certificatePem: null, found: false};
}

function getCertificateSubjectInSafeBag(safeContents, subjectCN) {
    for (let j = 0; j < safeContents.safeBags.length; j++) {
		const safeBag = safeContents.safeBags[j];

		if (safeBag.type === forge.pki.oids.certBag &&
            safeBag.cert.subject.getField('CN').value === subjectCN) {
            return {
                certificatePem: forge.pki.certificateToPem(safeBag.cert),
                found: true
            };
        }
    }

    return {certificatePem: null, found: false};
}

function isSafeBagWithAlias(nextBagId, safeBag, alias) {
    return (!nextBagId && safeBag.attributes &&
        safeBag.attributes.friendlyName &&
        safeBag.attributes.friendlyName[0] === alias) ||
        (nextBagId && nextBagId === safeBag.cert.subject.hash);
}

function checkFoundCertificateSafeBags(certificatePemChain, alias) {
    if (certificatePemChain.length === 0) {
		throw new Error(`Could not find any certificate chains with storage alias name '${alias}' ${IN_PKCS12}`);
    }
}
