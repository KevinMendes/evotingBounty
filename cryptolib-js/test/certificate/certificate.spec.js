/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, expr:true */
'use strict';

const { assert, expect } = require('chai');

const certificate = require('../../src/certificate');
const constants = require('../../src/certificate/constants');
const forge = require('node-forge');

const BigInteger = forge.jsbn.BigInteger;

const EPOCH_TIME_LENGTH = 10;

const _rootCertificatePem = '-----BEGIN CERTIFICATE-----MIIEKjCCAxKgAwIBAgIQQtWFdPN4NAvUIWyyJyUlbTANBgkqhkiG9w0BAQsFADBUMRAwDgYDVQQDDAdSb290IENOMRYwF' +
	'AYDVQQLDA1Sb290IE9yZyBVbml0MREwDwYDVQQKDAhSb290IE9yZzEVMBMGA1UEBhMMUm9vdCBDb3VudHJ5MB4XDTE0MDYxODEwMjMyOFoXDTE1MDYxODEwMjMyOFowVDEQMA4GA1UEAww' +
	'HUm9vdCBDTjEWMBQGA1UECwwNUm9vdCBPcmcgVW5pdDERMA8GA1UECgwIUm9vdCBPcmcxFTATBgNVBAYTDFJvb3QgQ291bnRyeTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBA' +
	'J4AidkWId1zr4IZgItjE9uv38eB8fIGJGEts2n/XNh/EJ7NYZyBzvKSJR83v7LVJjc4pup5crWyS0B5lQ7uuD3/XZ9QpQFtnGiqlKOKH45zsw3ekaHEAco07L2dZznBuQTGLTlhrmvkCFR' +
	'a8b8WYC+k90oUPvSd/9S4kA1Jlo9JDKHLer0SbjZCcVRXoLSBKmGWE0xSNfmNuZNONDRbtHvSA8A10AOtxKii9w464MXYzmPil7uM1Og1HC+FXCmkzLNfqQ31Om0jra3nLmrCpBJjRPX7s' +
	'vVnoxajRqpazVQnmJjjpzV7yNLwnR9W8OPwqanXcbxmTkrXMxfLxiVXDFUCAwEAAaOB9zCB9DAPBgNVHRMBAf8EBTADAQH/MDUGCCsGAQUFBwEBAQH/BCYwJDAiBggrBgEFBQcwAYYWaHR' +
	'0cDovL29jc3AudGhhd3RlLmNvbTA0BgNVHR8BAf8EKjAoMCagJKAihiBodHRwOi8vY3JsLnZlcmlzaWduLmNvbS9wY2EzLmNybDArBgNVHREBAf8EITAfpB0wGzEZMBcGA1UEAwwQUHJpd' +
	'mF0ZUxhYmVsMy0xNTAOBgNVHQ8BAf8EBAMCAQYwNwYDVR0lAQH/BC0wKwYIKwYBBQUHAwEGCCsGAQUFBwMCBgpghkgBhvhFAQgBBglghkgBhvhCBAEwDQYJKoZIhvcNAQELBQADggEBADm' +
	'tmjApZAXIkGLaZCdkRnhel53BtEdQnG990Oo/tBBboqy2ipum9ByTj3hNWJB3zuPN77rkrek9rbookNcCgVWhHtTk1lUpUK6ZohDsZh8k0MqIhkz+X+HiWGRsEOptjsCaknyWcWb4aXAev' +
	'MAQMPm/ktkpQ8AOxAq+gtieewWQZP3kGPhBBCfn8TGjdrn9+ymf8EIbAUFXQ8m+oWeNlrdWhqzRXwQbj4EDds1kZdTo0nCYUdH+XEBF9nMyhAxSQWzCKQQTRFWv1dr3dKapzfgrdH8wEgv' +
	'ptiBYCY62O5+3DxiNK/VWquHz6S5GqIwkmSPDPMUU/qK3SNG3xIL1U1k=-----END CERTIFICATE-----';
const _rootRsaPublicKeyPem = '-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAngCJ2RYh3XOvghmAi2MT26/fx4Hx8gYkYS2zaf9c2H8Qns1hn' +
	'IHO8pIlHze/stUmNzim6nlytbJLQHmVDu64Pf9dn1ClAW2caKqUo4ofjnOzDd6RocQByjTsvZ1nOcG5BMYtOWGua+QIVFrxvxZgL6T3ShQ+9J3/1LiQDUmWj0kMoct6vRJuNkJxVFegtIE' +
	'qYZYTTFI1+Y25k040NFu0e9IDwDXQA63EqKL3DjrgxdjOY+KXu4zU6DUcL4VcKaTMs1+pDfU6bSOtrecuasKkEmNE9fuy9WejFqNGqlrNVCeYmOOnNXvI0vCdH1bw4/CpqddxvGZOStczF' +
	'8vGJVcMVQIDAQAB-----END PUBLIC KEY-----';
const _rootSerialNumber = '88837713778966677489555326888277517677';
const _rootNotBefore = '1403087008';
const _rootNotAfter = '1434623008';
const _rootCn = 'Root CN';
const _rootOrgUnit = 'Root Org Unit';
const _rootOrg = 'Root Org';
const _rootCountry = 'Root Country';

const _leafCertificatePem = '-----BEGIN CERTIFICATE-----MIIEMzCCAxugAwIBAgIQRbaPaToIM+VS/d6etgYZ4jANBgkqhkiG9w0BAQsFADBUMRAwDgYDVQQDDAdSb290IENOMRYwF' +
	'AYDVQQLDA1Sb290IE9yZyBVbml0MREwDwYDVQQKDAhSb290IE9yZzEVMBMGA1UEBhMMUm9vdCBDb3VudHJ5MB4XDTE0MDYxODEwMjMyOFoXDTE1MDYxODEwMjMyOFowYDETMBEGA1UEAww' +
	'KU3ViamVjdCBDTjEZMBcGA1UECwwQU3ViamVjdCBPcmcgVW5pdDEUMBIGA1UECgwLU3ViamVjdCBPcmcxGDAWBgNVBAYTD1N1YmplY3QgQ291bnRyeTCCASIwDQYJKoZIhvcNAQEBBQADg' +
	'gEPADCCAQoCggEBAIJcqWVOOW539qKZ0SPmOdVaqLgabw0998SAfzrW8Cs8FaYvia4wRbX5N97RPbf3UkbO/QiveB0YQlnDoi2tqlj643mfUgYhMknK4SL0WVQReNcwYMEDbkbyQrCpgKp' +
	'WWhTSQ2cRD+K3qZpQZ9Qn1RZn615jiqsD+Re5fWbbnL3kc7H5hdclTxZFvEzLgd2KjIsspsqh5UvLowrLuSPLaYD28LsXTDmPeHzYUl62JGPCLl9YNc3av2dY5bjmkf1KiuFKZ27iWh/xd' +
	'FYAzYloenEw9AxRhJNG+9IucFOENy/0ul2UEb0rgA6Am4cASrhS+aVuZ/OuaC1W+Ut8LlXVmhsCAwEAAaOB9DCB8TAMBgNVHRMBAf8EAjAAMDUGCCsGAQUFBwEBAQH/BCYwJDAiBggrBgE' +
	'FBQcwAYYWaHR0cDovL29jc3AudGhhd3RlLmNvbTA0BgNVHR8BAf8EKjAoMCagJKAihiBodHRwOi8vY3JsLnZlcmlzaWduLmNvbS9wY2EzLmNybDArBgNVHREBAf8EITAfpB0wGzEZMBcGA' +
	'1UEAwwQUHJpdmF0ZUxhYmVsMy0xNTAOBgNVHQ8BAf8EBAMCBsAwNwYDVR0lAQH/BC0wKwYIKwYBBQUHAwEGCCsGAQUFBwMCBgpghkgBhvhFAQgBBglghkgBhvhCBAEwDQYJKoZIhvcNAQE' +
	'LBQADggEBAAWZDJD6bg4ohHewszrAbL2tdUNxhrwCgNaHUhwNK43kiLGH0U9innhL1i0jP1VHNkL1G/+ZCo1qzh/Usji/jtlurfAWtrXku6VRF9NP+itKOY5jJ91Ijkc7t4dgoeJq6iMHn' +
	'6JbDKIQ88r/Ikd0GdF04o5Qjqq1HlUVmqyIOHeHFla4i4tOxTyUBj34eE1No/xmaKYV1QtR1dqSHblR7OagEo7Dd3fXp7iSrKrXaN0Ef/6zeF3zjU5SMKcUcU9d3CbhS/CrGb+UGlqTXgz' +
	'PXQWESH9AqBNl67+HF3mYktDQOZYPT5WRO5IKSko2cy9pP9UCsLk4oU3xyOxacWDpk1k=-----END CERTIFICATE-----';
const _leafRsaPublicKeyPem = '-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAglypZU45bnf2opnRI+Y51VqouBpvDT33xIB/OtbwKzwVpi+Jr' +
	'jBFtfk33tE9t/dSRs79CK94HRhCWcOiLa2qWPrjeZ9SBiEyScrhIvRZVBF41zBgwQNuRvJCsKmAqlZaFNJDZxEP4repmlBn1CfVFmfrXmOKqwP5F7l9ZtucveRzsfmF1yVPFkW8TMuB3Yq' +
	'MiyymyqHlS8ujCsu5I8tpgPbwuxdMOY94fNhSXrYkY8IuX1g1zdq/Z1jluOaR/UqK4UpnbuJaH/F0VgDNiWh6cTD0DFGEk0b70i5wU4Q3L/S6XZQRvSuADoCbhwBKuFL5pW5n865oLVb5S' +
	'3wuVdWaGwIDAQAB-----END PUBLIC KEY-----';
const _leafSerialNumber = '92664638458902967092551440174596626914';
const _leafNotBefore = '1403087008';
const _leafNotAfter = '1434623008';
const _leafCn = 'Subject CN';
const _leafOrgUnit = 'Subject Org Unit';
const _leafOrg = 'Subject Org';
const _leafCountry = 'Subject Country';

let _certificateService;

function removeNewLineChars(str) {
    return str.replace(/(\r\n|\n|\r)/gm, '');
}

describe('The certificate module should be able to ...', function () {

    beforeEach(function () {
        _certificateService = certificate.newService();
    });

    describe('create a certificate service that should be able to ..', function () {
        it('load a root X.509 certificate into a new X509Certificate object and retrieve its contents', function () {
            // Load root certificate object.
			const rootCertificate = _certificateService.newX509Certificate(_rootCertificatePem);

			// Check that expected fields can be retrieved from root certificate.
			assert.isTrue(new BigInteger(rootCertificate.serialNumber, 16).equals(new BigInteger(_rootSerialNumber)));
			const notBefore = rootCertificate.notBefore.getTime().toString().substring(0, EPOCH_TIME_LENGTH);
			expect(notBefore).to.equal(_rootNotBefore);
			const notAfter = rootCertificate.notAfter.getTime().toString().substring(0, EPOCH_TIME_LENGTH);
			expect(notAfter).to.equal(_rootNotAfter);
            expect(rootCertificate.subjectCommonName).to.equal(_rootCn);
            expect(rootCertificate.subjectOrganizationalUnit).to.equal(_rootOrgUnit);
            expect(rootCertificate.subjectOrganization).to.equal(_rootOrg);
            expect(rootCertificate.subjectCountry).to.equal(_rootCountry);
            expect(rootCertificate.issuerCommonName).to.equal(_rootCn);
            expect(rootCertificate.issuerOrganizationalUnit).to.equal(_rootOrgUnit);
            expect(rootCertificate.issuerOrganization).to.equal(_rootOrg);
            expect(rootCertificate.issuerCountry).to.equal(_rootCountry);

            // Check that public key retrieved from root certificate is same as original.
			const rootPublicKeyPem = removeNewLineChars(rootCertificate.publicKey);
			expect(rootPublicKeyPem).to.equal(_rootRsaPublicKeyPem);
			const rootPublicKey = forge.pki.publicKeyFromPem(rootPublicKeyPem);
			const rootPublicKeyOrig = forge.pki.publicKeyFromPem(_rootRsaPublicKeyPem);
			expect(rootPublicKey.e).to.deep.equal(rootPublicKeyOrig.e);
            expect(rootPublicKey.n).to.deep.equal(rootPublicKeyOrig.n);

            // Check that expected basic constraints flags can be retrieved from
            // certificate.
			const basicConstraints = rootCertificate.basicConstraints;
			assert.isTrue(basicConstraints.ca);

            // Check that expected key usage flags can be retrieved from
            // certificate.
			const keyUsageExtension = rootCertificate.keyUsageExtension;
			assert.isFalse(keyUsageExtension.digitalSignature);
			assert.isFalse(keyUsageExtension.nonRepudiation);
			assert.isFalse(keyUsageExtension.keyEncipherment);
			assert.isFalse(keyUsageExtension.dataEncipherment);
			assert.isFalse(keyUsageExtension.keyAgreement);
			assert.isTrue(keyUsageExtension.keyCertSign);
			assert.isTrue(keyUsageExtension.crlSign);
			assert.isFalse(keyUsageExtension.encipherOnly);
			assert.isFalse(keyUsageExtension.decipherOnly);

            // Check that root certificate is self-signed.
			assert.isTrue(rootCertificate.verify(_rootCertificatePem));

            // Serialize root certificate to PEM format.
			const rootCertificatePem = rootCertificate.toPem();
			const offset = rootCertificatePem.indexOf('\n') + 1;
			expect(rootCertificatePem.indexOf('\n', offset)).to.equal(offset + constants.PEM_LINE_LENGTH + 1);
            expect(removeNewLineChars(rootCertificatePem)).to.equal(_rootCertificatePem);
        });

        it('load a leaf X.509 certificate into a new X509Certificate object and retrieve its contents', function () {
            // Load leaf certificate.
			const leafCertificate = _certificateService.newX509Certificate(_leafCertificatePem);

			// Check that expected fields can be retrieved from leaf certificate.
			assert.isTrue(new BigInteger(leafCertificate.serialNumber, 16).equals(new forge.jsbn.BigInteger(_leafSerialNumber)));
			const notBefore = leafCertificate.notBefore.getTime().toString().substring(0, EPOCH_TIME_LENGTH);
			expect(notBefore).to.equal(_leafNotBefore);
			const notAfter = leafCertificate.notAfter.getTime().toString().substring(0, EPOCH_TIME_LENGTH);
			expect(notAfter).to.equal(_leafNotAfter);
            expect(leafCertificate.subjectCommonName).to.equal(_leafCn);
            expect(leafCertificate.subjectOrganizationalUnit).to.equal(_leafOrgUnit);
            expect(leafCertificate.subjectOrganization).to.equal(_leafOrg);
            expect(leafCertificate.subjectCountry).to.equal(_leafCountry);
            expect(leafCertificate.issuerCommonName).to.equal(_rootCn);
            expect(leafCertificate.issuerOrganizationalUnit).to.equal(_rootOrgUnit);
            expect(leafCertificate.issuerOrganization).to.equal(_rootOrg);
            expect(leafCertificate.issuerCountry).to.equal(_rootCountry);

            // Check that public key retrieved from certificate is same as original.
			const leafPublicKeyPem = removeNewLineChars(leafCertificate.publicKey);
			expect(leafPublicKeyPem).to.equal(_leafRsaPublicKeyPem);
			const leafPublicKey = forge.pki.publicKeyFromPem(leafPublicKeyPem);
			const leafPublicKeyOrig = forge.pki.publicKeyFromPem(_leafRsaPublicKeyPem);
			expect(leafPublicKey.e).to.deep.equal(leafPublicKeyOrig.e);
            expect(leafPublicKey.n).to.deep.equal(leafPublicKeyOrig.n);

            // Check that expected basic constraints flags can be retrieved from certificate.
			const basicConstraints = leafCertificate.basicConstraints;
			assert.isFalse(basicConstraints.ca);

            // Check that expected key usage flags can be retrieved from certificate.
			const keyUsageExtension = leafCertificate.keyUsageExtension;
			assert.isTrue(keyUsageExtension.digitalSignature);
			assert.isTrue(keyUsageExtension.nonRepudiation);
			assert.isFalse(keyUsageExtension.keyEncipherment);
			assert.isFalse(keyUsageExtension.dataEncipherment);
			assert.isFalse(keyUsageExtension.keyAgreement);
			assert.isFalse(keyUsageExtension.keyCertSign);
			assert.isFalse(keyUsageExtension.crlSign);
			assert.isFalse(keyUsageExtension.encipherOnly);
			assert.isFalse(keyUsageExtension.decipherOnly);

            // Verify that leaf certificate was signed by root certificate.
			const rootCertificate = _certificateService.newX509Certificate(_rootCertificatePem);
			assert.isTrue(rootCertificate.verify(_leafCertificatePem));

            // Serialize leaf certificate to PEM format.
			const leafCertificatePem = leafCertificate.toPem();
			const offset = leafCertificatePem.indexOf('\n') + 1;
			expect(leafCertificatePem.indexOf('\n', offset)).to.equal(offset + constants.PEM_LINE_LENGTH + 1);
            expect(removeNewLineChars(leafCertificatePem)).to.equal(_leafCertificatePem);
        });
    });
});
