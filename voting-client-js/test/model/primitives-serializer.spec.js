/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {PlaintextEqualityProof} = require("crypto-primitives-ts/lib/cjs/zeroknowledgeproofs/plaintext_equality_proof");
const {ExponentiationProof} = require("crypto-primitives-ts/lib/cjs/zeroknowledgeproofs/exponentiation_proof");
const {ZqGroup} = require("crypto-primitives-ts/lib/cjs/math/zq_group");
const {ZqElement} = require("crypto-primitives-ts/lib/cjs/math/zq_element");
const {GqGroup} = require("crypto-primitives-ts/lib/cjs/math/gq_group");
const {GroupVector} = require("crypto-primitives-ts/lib/cjs/group_vector");
const {GqElement} = require("crypto-primitives-ts/lib/cjs/math/gq_element");
const {ImmutableBigInteger} = require("crypto-primitives-ts/lib/cjs/immutable_big_integer");
const {ElGamalMultiRecipientCiphertext} = require("crypto-primitives-ts/lib/cjs/elgamal/elgamal_multi_recipient_ciphertext");
const {serializeElGamalCiphertext, serializeExponentiationProof, serializePlaintextEqualityProof} = require('../../src/model/primitives-serializer');

describe('Primitives serializer', function () {
	'use strict';

	const testData = require('./mocks/primitives.json');
	const g = ImmutableBigInteger.fromNumber(testData.g);
	const p = ImmutableBigInteger.fromString(testData.p);
	const q = ImmutableBigInteger.fromString(testData.q);
	const bigGqGroup = new GqGroup(p, q, g);

	it('should serialize ElGamalCiphertext', function () {
		const smallP = ImmutableBigInteger.fromNumber(testData.smallP);
		const smallQ = ImmutableBigInteger.fromNumber(testData.smallQ);
		const smallGqGroup = new GqGroup(smallP, smallQ, g);

		const gFour = GqElement.fromValue(ImmutableBigInteger.fromNumber(testData.elGamalCiphertext.phis[0]), smallGqGroup);
		const gEight = GqElement.fromValue(ImmutableBigInteger.fromNumber(testData.elGamalCiphertext.phis[1]), smallGqGroup);
		const gThirteen = GqElement.fromValue(ImmutableBigInteger.fromNumber(testData.elGamalCiphertext.gamma), smallGqGroup);
		const ciphertext = ElGamalMultiRecipientCiphertext.create(gThirteen, [gFour, gEight]);

		const expectedCiphertext = JSON.stringify(testData.elGamalCiphertext.expected);

		expect(expectedCiphertext).toEqual(serializeElGamalCiphertext(ciphertext));
	});

	it('should serialize ExponentiationProof', function () {
		const zqGroup = ZqGroup.sameOrderAs(bigGqGroup);
		const eBigInteger = ImmutableBigInteger.fromString(testData.exponentiationProof.e);
		const e = ZqElement.create(eBigInteger, zqGroup);
		const zBigInteger = ImmutableBigInteger.fromString(testData.exponentiationProof.z);
		const z = ZqElement.create(zBigInteger, zqGroup);
		const proof = new ExponentiationProof(e, z);

		const expectedProof = JSON.stringify(testData.exponentiationProof.expected);

		expect(expectedProof).toEqual(serializeExponentiationProof(proof));
	});

	it('should serialize PlaintextEqualityProof', function () {
		const zqGroup = ZqGroup.sameOrderAs(bigGqGroup);
		const eBigInteger = ImmutableBigInteger.fromString(testData.plaintextEqualityProof.e);
		const e = ZqElement.create(eBigInteger, zqGroup);
		const z1BigInteger = ImmutableBigInteger.fromString(testData.plaintextEqualityProof.z[0]);
		const z1 = ZqElement.create(z1BigInteger, zqGroup);
		const z2BigInteger = ImmutableBigInteger.fromString(testData.plaintextEqualityProof.z[1]);
		const z2 = ZqElement.create(z2BigInteger, zqGroup);
		const z = new GroupVector([z1, z2])
		const proof = new PlaintextEqualityProof(e, z);

		const expectedProof = JSON.stringify(testData.plaintextEqualityProof.expected);

		expect(expectedProof).toEqual(serializePlaintextEqualityProof(proof));
	});
});


