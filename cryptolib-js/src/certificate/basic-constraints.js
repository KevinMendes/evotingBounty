/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

module.exports = BasicConstraints;

/**
 * @class BasicConstraints
 * @classdesc Encapsulates the basic constraints of an X.509 certificate. This
 *            object is instantiated internally by the method
 *            {@link CertificateService.newX509Certificate} and made available
 *            as a property of the returned {@link X509Certificate} object.
 * @property {boolean} ca <code>true</code> if the CA flag is set,
 *           <code>false</code> otherwise.
 */
function BasicConstraints(constraints) {
    return Object.freeze({ca: constraints.cA});
}
