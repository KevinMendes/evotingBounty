/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const { expect } = require('chai');

const certificate = require('../../src/certificate');

const _certificatePem = '-----BEGIN CERTIFICATE-----MIIEKjCCAxKgAwIBAgIQQtWFdPN4NAvUIWyyJyUlbTANBgkqhkiG9w0BAQsFADBUMRAwDgYDVQQDDAdSb290IENOMRYwFAYDV' +
	'QQLDA1Sb290IE9yZyBVbml0MREwDwYDVQQKDAhSb290IE9yZzEVMBMGA1UEBhMMUm9vdCBDb3VudHJ5MB4XDTE0MDYxODEwMjMyOFoXDTE1MDYxODEwMjMyOFowVDEQMA4GA1UEAwwHUm9' +
	'vdCBDTjEWMBQGA1UECwwNUm9vdCBPcmcgVW5pdDERMA8GA1UECgwIUm9vdCBPcmcxFTATBgNVBAYTDFJvb3QgQ291bnRyeTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAJ4Ai' +
	'dkWId1zr4IZgItjE9uv38eB8fIGJGEts2n/XNh/EJ7NYZyBzvKSJR83v7LVJjc4pup5crWyS0B5lQ7uuD3/XZ9QpQFtnGiqlKOKH45zsw3ekaHEAco07L2dZznBuQTGLTlhrmvkCFRa8b8' +
	'WYC+k90oUPvSd/9S4kA1Jlo9JDKHLer0SbjZCcVRXoLSBKmGWE0xSNfmNuZNONDRbtHvSA8A10AOtxKii9w464MXYzmPil7uM1Og1HC+FXCmkzLNfqQ31Om0jra3nLmrCpBJjRPX7svVno' +
	'xajRqpazVQnmJjjpzV7yNLwnR9W8OPwqanXcbxmTkrXMxfLxiVXDFUCAwEAAaOB9zCB9DAPBgNVHRMBAf8EBTADAQH/MDUGCCsGAQUFBwEBAQH/BCYwJDAiBggrBgEFBQcwAYYWaHR0cDo' +
	'vL29jc3AudGhhd3RlLmNvbTA0BgNVHR8BAf8EKjAoMCagJKAihiBodHRwOi8vY3JsLnZlcmlzaWduLmNvbS9wY2EzLmNybDArBgNVHREBAf8EITAfpB0wGzEZMBcGA1UEAwwQUHJpdmF0Z' +
	'UxhYmVsMy0xNTAOBgNVHQ8BAf8EBAMCAQYwNwYDVR0lAQH/BC0wKwYIKwYBBQUHAwEGCCsGAQUFBwMCBgpghkgBhvhFAQgBBglghkgBhvhCBAEwDQYJKoZIhvcNAQELBQADggEBADmtmjA' +
	'pZAXIkGLaZCdkRnhel53BtEdQnG990Oo/tBBboqy2ipum9ByTj3hNWJB3zuPN77rkrek9rbookNcCgVWhHtTk1lUpUK6ZohDsZh8k0MqIhkz+X+HiWGRsEOptjsCaknyWcWb4aXAevMAQM' +
	'Pm/ktkpQ8AOxAq+gtieewWQZP3kGPhBBCfn8TGjdrn9+ymf8EIbAUFXQ8m+oWeNlrdWhqzRXwQbj4EDds1kZdTo0nCYUdH+XEBF9nMyhAxSQWzCKQQTRFWv1dr3dKapzfgrdH8wEgvptiB' +
	'YCY62O5+3DxiNK/VWquHz6S5GqIwkmSPDPMUU/qK3SNG3xIL1U1k=-----END CERTIFICATE-----';

const _validationData = {
	issuer: {
		commonName: 'Test CN',
		organizationalUnit: 'Test Org Unit',
		organization: 'Test Org',
		country: 'Test Country'
	}
};

const _nonString = 999;
const _emptyString = [];
const _nonPemString = 'Not a PEM string';
const _emptyObject = {};
const _nonTwoDimensionalArray = '';
const _emptyTwoDimensionalArray = [];

let _certificateService;
let _certificateValidator;

describe('The certificate module should be able to ...', function () {

    beforeEach(function () {
        _certificateService = certificate.newService();
        _certificateValidator = _certificateService.newValidator();
    });

    describe('create a certificate service that should be able to ..', function () {
        it('throw an error when creating a new X509Certificate object, using invalid input data',
            function () {
                expect(function () {
                    _certificateService.newX509Certificate();
                }).to.throw();

                expect(function () {
                    _certificateService.newX509Certificate(undefined);
                }).to.throw();

                expect(function () {
                    _certificateService.newX509Certificate(null);
                }).to.throw();

                expect(function () {
                    _certificateService.newX509Certificate(_nonString);
                }).to.throw();

                expect(function () {
                    _certificateService.newX509Certificate(_emptyString);
                }).to.throw();

                expect(function () {
                    _certificateService.newX509Certificate(_nonPemString);
                }).to.throw();
            });

        describe(
            'create a new X509Certificate object that should be able to',
            function () {
                it('throw an error when verifying the signature of another X.509 certificate, using invalid input data',
                    function () {
						const certificate =
							_certificateService.newX509Certificate(_certificatePem);

						expect(function () {
                            certificate.verify();
                        }).to.throw();

                        expect(function () {
                            certificate.verify(undefined);
                        }).to.throw();

                        expect(function () {
                            certificate.verify(null);
                        }).to.throw();

                        expect(function () {
                            certificate.verify(_nonString);
                        }).to.throw();

                        expect(function () {
                            certificate.verify(_nonPemString);
                        }).to.throw();
                    });
            });

        describe(
            'create a CertificateValidator object that should be able to ..',
            function () {
                it('throw an error when validating a certificate, using an invalid certificate',
                    function () {
                        expect(function () {
                            _certificateValidator.validate(undefined, _validationData);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validate(null, _validationData);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validate(_nonString, _validationData);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validate(_emptyString, _validationData);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validate(_nonPemString, _validationData);
                        }).to.throw();
                    });

                it('throw an error when validating a certificate, using invalid validation data',
                    function () {
                        expect(function () {
                            _certificateValidator.validate(_certificatePem);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validate(_certificatePem, undefined);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validate(_certificatePem, null);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validate(_certificatePem, _emptyObject);
                        }).to.throw();
                    });

                it('throw an error when validating a certificate chain, using invalid input data',
                    function () {
                        expect(function () {
                            _certificateValidator.validateChain();
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validateChain(undefined);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validateChain(null);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.validateChain(_emptyObject);
                        }).to.throw();
                    });

                it('throw an error when flattening the failed validations of a certificate chain, using invalid input data',
                    function () {
                        expect(function () {
                            _certificateValidator.flattenFailedValidations();
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.flattenFailedValidations(undefined);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.flattenFailedValidations(null);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.flattenFailedValidations(
                                _nonTwoDimensionalArray);
                        }).to.throw();

                        expect(function () {
                            _certificateValidator.flattenFailedValidations(
                                _emptyTwoDimensionalArray);
                        }).to.throw();
                    });
            });
    });
});
