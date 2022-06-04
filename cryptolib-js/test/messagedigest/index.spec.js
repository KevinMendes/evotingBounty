/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true, jasmine:true */
'use strict';

const { expect } = require('chai');

const messageDigest = require('../../src/messagedigest');
const cryptoPolicy = require('../../src/cryptopolicy');
const bitwise = require('../../src/bitwise');
const codec = require('../../src/codec');

describe('The message digest module should be able to ...', function () {
	const DATA = 'Ox2fUJq1gAbX';
	const OTHER_DATA_1 = DATA + 'a';
	const OTHER_DATA_2 = DATA + 'b';

	let _data;
    let _otherData1;
    let _otherData2;
    let _service;

    beforeEach(function () {
        _data = codec.utf8Encode(DATA);
        _otherData1 = codec.utf8Encode(OTHER_DATA_1);
        _otherData2 = codec.utf8Encode(OTHER_DATA_2);
        _service = messageDigest.newService();
    });

    describe('create a message digest service that should be able to ..', function () {
        describe('create a message digester that should be able to', function () {
            it('generate digests that are deterministic', function () {
                // Pass the digester some data.
                let digester = _service.newDigester();

				const firstMsgDigest = digester.digest(_data);

				// Pass another digester the same data.
                digester = _service.newDigester();
				const secondMsgDigest = digester.digest(_data);

				// Verify that both processes have produced the same result.
                expect(secondMsgDigest).deep.equal(firstMsgDigest);

                // Pass yet another digester slightly altered data.
                digester = _service.newDigester();
				const thirdMsgDigest = digester.digest(_otherData1);

				// Verify that new message digest does not match the first two.
                expect(thirdMsgDigest).to.not.deep.equal(firstMsgDigest);
            });

            it('generate a SHA256 hash', function () {
				const policy = cryptoPolicy.newInstance();
				policy.primitives.messageDigest.algorithm =
                    cryptoPolicy.options.primitives.messageDigest.algorithm.SHA256;
				const service = messageDigest.newService({policy: policy});
				const digester = service.newDigester();
				const digest = digester.digest(_data);

				expect(digest).to.deep.equal(codec
                    .hexDecode('229829d4d6cdaded09e053426fbcf9e5' +
                        '690fb2ba60fc846ed845557a605baf00'));
            });

            it('generate a SHA512/224 hash', function () {
				const policy = cryptoPolicy.newInstance();
				policy.primitives.messageDigest.algorithm =
                    cryptoPolicy.options.primitives.messageDigest.algorithm.SHA512_224;
				const service = messageDigest.newService({policy: policy});
				const digester = service.newDigester();
				const digest = digester.digest(_data);

				expect(digest).to.deep.equal(codec
                    .hexDecode('eace70365d08dbf7ffa0da00365d' +
                        '94a65f5a994e73b2118fae1bcfd8'));
            });

            it('generate a SHA256 hash by updating the digester', function () {
				const policy = cryptoPolicy.newInstance();
				policy.primitives.messageDigest.algorithm =
                    cryptoPolicy.options.primitives.messageDigest.algorithm.SHA256;
				const service = messageDigest.newService({policy: policy});
				const digester = service.newDigester();

				digester.update('SwissPost');
                digester.update('SHA256');
				const digest = digester.digest();

				expect(digest).to.deep.equal(codec
                    .hexDecode('9b500fc9735e94dd67e87f7c11273eb60fa26df9b12a0f9d2ef72822f80bc43b'));
            });

            it('generate a digest from multiple data parts', function () {
				const dataParts = [_data, _otherData1, _otherData2];

				let digester = _service.newDigester();
                for (let i = 0; i < dataParts.length; i++) {
                    digester.update(dataParts[i]);
                }
                let firstMsgDigest = digester.digest();

                digester = _service.newDigester();
                let secondMsgDigest = digester.digest(
                    bitwise.concatenate(_data, _otherData1, _otherData2));
                expect(secondMsgDigest).to.deep.equal(firstMsgDigest);

                digester = _service.newDigester();
                for (let j = 0; j < (dataParts.length - 1); j++) {
                    digester.update(dataParts[j]);
                }
                firstMsgDigest = digester.digest(_otherData2);

                digester = _service.newDigester();
                secondMsgDigest = digester.digest(
                    bitwise.concatenate(_data, _otherData1, _otherData2));
                expect(secondMsgDigest).to.deep.equal(firstMsgDigest);
            });

            it('generate a digest from multiple data parts, using method chaining',
                function () {
					const digester = _service.newDigester();
					const firstMsgDigest = digester.digest(
						bitwise.concatenate(_data, _otherData1, _otherData2));

					let secondMsgDigest = _service.newDigester()
                        .update(_data)
                        .update(_otherData1)
                        .update(_otherData2)
                        .digest();
                    expect(secondMsgDigest).to.deep.equal(firstMsgDigest);

                    secondMsgDigest = _service.newDigester()
                        .update(_data)
                        .update(_otherData1)
                        .digest(_otherData2);
                    expect(secondMsgDigest).to.deep.equal(firstMsgDigest);
                });

            it('generate a digest with the expected value when using data of type string',
                function () {
					const digester = _service.newDigester();

					const firstMsgDigest =
						digester.update(_data).update(_otherData1).digest(_otherData2);

					const secondMsgDigest =
						digester.update(DATA).update(OTHER_DATA_1).digest(OTHER_DATA_2);

					expect(secondMsgDigest).to.deep.equal(firstMsgDigest);
                });

            it('be automatically reinitialized after digesting', function () {
				const digester = _service.newDigester();

				const firstMsgDigest = digester.digest(_data);
				const secondMsgDigest = digester.digest(_data);

				expect(secondMsgDigest).to.deep.equal(firstMsgDigest);
            });

            it('throw an error upon creation if an unsupported crytographic policy was provided to the service',
                function () {
					const policy = cryptoPolicy.newInstance();
					policy.primitives.messageDigest.algorithm = 'SHA0';
					const service = messageDigest.newService({policy: policy});

					expect(function () {
                        service.newDigester();
                    }).to.throw();
                });

            it('throw an error when generating a digest without either providing data or having previously updated the digester with some data',
                function () {
					const digester = _service.newDigester();

					expect(function () {
                        digester.digest();
                    }).to.throw();
                });
        });
    });
});
