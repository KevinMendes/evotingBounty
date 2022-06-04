/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

module.exports = KeyUsageExtension;

/**
 * @class KeyUsageExtension
 * @classdesc Encapsulates the key usage extension flags of an X.509
 *            certificate. This object is instantiated internally by the method
 *            {@link CertificateService.newX509Certificate} and made available
 *            as a property of the returned {@link X509Certificate} object.
 * @property {boolean} digitalSignature <code>true</code> if the
 *           <code>digital signature</code> flag is set, <code>false</code>
 *           otherwise.
 * @property {boolean} nonRepudiation <code>true</code> if the
 *           <code>non-repudiation</code> flag is set, <code>false</code>
 *           otherwise.
 * @property {boolean} keyEncipherment <code>true</code> if the
 *           <code>key encipherment</code> flag is set, <code>false</code>
 *           otherwise.
 * @property {boolean} dataEncipherment <code>true</code> if the
 *           <code>data encipherment</code> flag is set, <code>false</code>
 *           otherwise.
 * @property {boolean} keyAgreement <code>true</code> if the
 *           <code>key agreement</code> flag is set, <code>false</code>
 *           otherwise.
 * @property {boolean} keyCertSign <code>true</code> if the
 *           <code>key certificate sign</code> flag is set, <code>false</code>
 *           otherwise.
 * @property {boolean} crlSign <code>true</code> if the <code>CRL sign</code>
 *           flag is set, <code>false</code> otherwise.
 * @property {boolean} encipherOnly <code>true</code> if the
 *           <code>encipher only</code> flag is set, <code>false</code>
 *           otherwise.
 * @property {boolean} decipherOnly <code>true</code> if the
 *           <code>decipher only</code> flag is set, <code>false</code>
 *           otherwise.
 */
function KeyUsageExtension(extension) {
    return Object.freeze({
        digitalSignature: extension.digitalSignature,
        nonRepudiation: extension.nonRepudiation,
        keyEncipherment: extension.keyEncipherment,
        dataEncipherment: extension.dataEncipherment,
        keyAgreement: extension.keyAgreement,
        keyCertSign: extension.keyCertSign,
        crlSign: extension.cRLSign,
        encipherOnly: extension.encipherOnly,
        decipherOnly: extension.decipherOnly
    });
}
