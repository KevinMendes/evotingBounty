/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* gloabl OV */

const certificateService = require('cryptolib-js/src/certificate').newService();

module.exports = (function () {
	'use strict';

	return function (
		certificateAuth,
		certificates,
	) {
		const platformRootCA = OV.config()['platformRootCA'];

		const getSubject = function (cert) {
			const cryptoX509Cert = certificateService.newX509Certificate(cert);
			return {
				commonName: cryptoX509Cert.subjectCommonName,
				organization: cryptoX509Cert.subjectOrganization,
				organizationalUnit: cryptoX509Cert.subjectOrganizationalUnit,
				country: cryptoX509Cert.subjectCountry,
			};
		};

		const getSubjects = function (certs) {
			const subjects = [];
			for (let i = 0; i < certs.length; i++) {
				const subject = getSubject(certs[i]);
				subjects.push(subject);
			}
			return subjects;
		};

		const validateChain = function (cert, certs, rootCert, keyType) {
			const chain = {
				leaf: {
					pem: cert,
					keyType: keyType,
					subject: getSubject(cert),
				},
				intermediates: {
					pems: certs,
					subjects: getSubjects(certs),
				},
				root: {
					pem: rootCert,
				},
			};

			const validationResult = certificateService
				.newValidator()
				.validateChain(chain);

			for (let i = 0; i < validationResult.length; i++) {
				if (validationResult[i].length !== 0) {
					throw new Error(
						'Certificate chain validation: [' +
						chain.leaf.subject.commonName +
						', ' +
						chain.certificates.subjects +
						', ' +
						getSubject(rootCert).commonName +
						'] failed: ' +
						validationResult,
					);
				}
			}
		};

		// [Election Root CA]
		validateChain(
			certificates.electionRootCA,
			[],
			certificates.electionRootCA,
			'CA',
		);

		// [Authentication Token Certificate, Services CA, Election Root CA]
		validateChain(
			certificates.authenticationTokenSignerCert,
			[certificates.servicesCA],
			certificates.electionRootCA,
			'Sign',
		);

		// [AB Certificate, TENANT CA, Platform Root CA]
		validateChain(
			certificates.adminBoard,
			[certificates.tenantCA],
			platformRootCA,
			'Sign',
		);

		// [Credentials CA, Election Root CA]
		validateChain(
			certificates.credentialsCA,
			[],
			certificates.electionRootCA,
			'CA',
		);

		// [CredentialID Authentication Certificate, Credentials CA, Election Root CA]
		validateChain(
			certificateAuth,
			[certificates.credentialsCA],
			certificates.electionRootCA,
			'Sign',
		);
	};
})();
