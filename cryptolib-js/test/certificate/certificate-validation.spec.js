/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const { expect } = require('chai');

const certificate = require('../../src/certificate');

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

const _issuerValidationData = {
	issuer: {
		commonName: 'Root CN',
		organizationalUnit: 'Root Org Unit',
		organization: 'Root Org',
		country: 'Root Country'
	}
};

const _subjectValidationData = {
	subject: {
		commonName: 'Subject CN',
		organizationalUnit: 'Subject Org Unit',
		organization: 'Subject Org',
		country: 'Subject Country'
	}
};

let _certificateValidator;

function clone(object) {
    return JSON.parse(JSON.stringify(object));
}

const describe1 = 'The certificate module should be able to ...';
const describe2 = 'create a certificate service that should be able to ...';
const describe3 = 'create a CertificateValidator object that should be able to ...';

describe(describe1, function () {

    beforeEach(function () {
        _certificateValidator = certificate.newService().newValidator();
    });

    describe(describe2, function () {
        describe(describe3, function () {

            it('successfully validate a certificate that contains the expected issuer DN.',
                function () {
					const validations = _certificateValidator.validate(_leafCertificatePem, _issuerValidationData);
					expect(validations.length).to.equal(0);
                });

            it('unsuccessfully validate a certificate that contains an unexpected issuer common name',
                function () {
					const validationData = clone(_issuerValidationData);
					validationData.issuer.commonName = 'Different Root CN';

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).to.deep.equal(['ISSUER']);
                });

            it('unsuccessfully validate a certificate that contains an unexpected issuer organizational unit',
                function () {
					const validationData = clone(_issuerValidationData);
					validationData.issuer.organizationalUnit = 'Different Root Org Unit';

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['ISSUER']);
                });

            it('unsuccessfully validate a certificate that contains an unexpected issuer organization',
                function () {
					const validationData = clone(_issuerValidationData);
					validationData.issuer.organization = 'Different Root Org';

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['ISSUER']);
                });

            it('unsuccessfully validate a certificate that contains an unexpected issuer country',
                function () {
					const validationData = clone(_issuerValidationData);
					validationData.issuer.country = 'Different Root Country';

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['ISSUER']);
                });
        });
    });
});

describe(describe1, function () {

    beforeEach(function () {
        _certificateValidator = certificate.newService().newValidator();
    });

    describe(describe2, function () {
        describe(describe3, function () {

            it('successfully validate a certificate that contains the expected subject DN.',
                function () {
					const validations = _certificateValidator.validate(_leafCertificatePem, _subjectValidationData);
					expect(validations.length).to.equal(0);
                });

            it('unsuccessfully validate a certificate that contains an unexpected subject common name',
                function () {
					const validationData = clone(_subjectValidationData);
					validationData.subject.commonName = 'Different Subject CN';

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).to.deep.equal(['SUBJECT']);
                });

            it('unsuccessfully validate a certificate that contains an unexpected subject organizational unit',
                function () {
					const validationData = clone(_subjectValidationData);
					validationData.subject.organizationalUnit = 'Different Subject Org Unit';

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).to.deep.equal(['SUBJECT']);
                });

            it('unsuccessfully validate a certificate that contains an unexpected subject organization',
                function () {
					const validationData = clone(_subjectValidationData);
					validationData.subject.organization = 'Different Subject Org';

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).to.deep.equal(['SUBJECT']);
                });

            it('unsuccessfully validate a certificate that contains an unexpected subject country',
                function () {
					const validationData = clone(_subjectValidationData);
					validationData.subject.country = 'Different Subject Country';

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['SUBJECT']);
                });
        });
    });
});

describe(describe1, function () {

    beforeEach(function () {
        _certificateValidator = certificate.newService().newValidator();
    });

    describe(describe2, function () {
        describe(describe3, function () {

            it('successfully validate a certificate when the validation time is within the certificate validity period',
                function () {
					const validationData = {time: '2014-12-25\'T\'00:00:00Z'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations.length).to.equal(0);
                });

            it('successfully validate a certificate when the validation time is exactly the certificate starting time of validity',
                function () {
					const validationData = {time: '2014-06-18\'T\'10:23:28Z'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations.length).to.equal(0);
                });

            it('successfully validate a certificate when the validation time is exactly the certificate ending time of validity',
                function () {
					const validationData = {time: '2015-06-18\'T\'10:23:28Z'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations.length).to.equal(0);
                });

            it('unsuccessfully validate a certificate when the validation time is before the certificate starting time of validity',
                function () {
					const validationData = {time: '2014-05-18\'T\'10:23:28Z'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['TIME']);
                });

            it('unsuccessfully validate a certificate when the validation time is after the certificate ending time of validity',
                function () {
					const validationData = {time: '2015-07-18\'T\'10:23:28Z'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['TIME']);
                });

            it('unsuccessfully validate a certificate when the validation time is specified as a single character',
                function () {
					const validationData = {time: 'a'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['TIME']);
                });

            it('unsuccessfully validate a certificate when the validation time is specified as three characters',
                function () {
					const validationData = {time: 'abc'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['TIME']);
                });

            it('unsuccessfully validate a certificate whose validation time is specified as twenty characters',
                function () {
					const validationData = {time: 'aaaaaaaaaaaaaaaaaaaa'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['TIME']);
                });
        });
    });
});

describe(describe1, function () {

    beforeEach(function () {
        _certificateValidator = certificate.newService().newValidator();
    });

    describe(describe2, function () {
        describe(describe3, function () {

            it('successfully validate a certificate of key type Sign when the validation key type is specified as Sign',
                function () {
					const validationData = {keyType: 'Sign'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations.length).to.equal(0);
                });

            it('successfully validate a certificate of key type CA when the validation key type is specified as CA',
                function () {
					const validationData = {keyType: 'CA'};

					const validations = _certificateValidator.validate(_rootCertificatePem, validationData);
					expect(validations.length).to.equal(0);
                });

            it('unsuccessfully validate a leaf certificate when the validation key type is specified as CA',
                function () {
					const validationData = {keyType: 'CA'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['KEY_TYPE']);
                });

            it('unsuccessfully validate a certificate of key type Sign when the validation key type is specified as Encryption',
                function () {
					const validationData = {keyType: 'Encryption'};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['KEY_TYPE']);
                });

            it('unsuccessfully validate a certificate of key type CA when the validation key type is specified as Sign',
                function () {
					const validationData = {keyType: 'Sign'};

					const validations = _certificateValidator.validate(_rootCertificatePem, validationData);
					expect(validations).deep.equal(['KEY_TYPE']);
                });

            it('unsuccessfully validate a certificate  of key type CA when the validation key type is specified as Encryption',
                function () {
					const validationData = {keyType: 'Encryption'};

					const validations = _certificateValidator.validate(_rootCertificatePem, validationData);
					expect(validations).deep.equal(['KEY_TYPE']);
                });
        });
    });
});

describe(describe1, function () {

    beforeEach(function () {
        _certificateValidator = certificate.newService().newValidator();
    });

    describe(describe2, function () {
        describe(describe3, function () {

            it('successfully validate a root certificate when the validation signing certificate is specified as the root certificate itself',
                function () {
					const validationData = {issuerCertificatePem: _rootCertificatePem};

					const validations = _certificateValidator.validate(_rootCertificatePem, validationData);
					expect(validations.length).to.equal(0);
                });

            it('successfully validate a leaf certificate when the validation signing certificate is specified as its parent certificate',
                function () {
					const validationData = {issuerCertificatePem: _rootCertificatePem};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations.length).to.equal(0);
                });

            it('unsuccessfully validate a root certificate when the validation signing certificate is specified as another certificate',
                function () {
					const validationData = {issuerCertificatePem: _leafCertificatePem};

					const validations = _certificateValidator.validate(
						_rootCertificatePem, validationData);
					expect(validations).deep.equal(['SIGNATURE']);
                });

            it('unsuccessfully validate a leaf certificate when the validation signing certificate is specified as the leaf certificate itself',
                function () {
					const validationData = {issuerCertificatePem: _leafCertificatePem};

					const validations = _certificateValidator.validate(_leafCertificatePem, validationData);
					expect(validations).deep.equal(['SIGNATURE']);
                });
        });
    });
});
