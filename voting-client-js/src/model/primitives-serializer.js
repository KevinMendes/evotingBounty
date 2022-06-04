/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {checkNotNull} = require("crypto-primitives-ts/lib/cjs/validation/preconditions");

module.exports = (function () {
	'use strict';

	const HEX_PREFIX = "0x";

	/**
	 * Serializes a {GroupElement} to a standalone string, i.e. by itself it does not return a valid JSON. We consider the GroupElement as a
	 * primitive and not an object, hence it cannot be directly serialized to a JSON with this method.
	 *
	 * @param element {GroupElement}, the element to serialize. Must be not null.
	 * @returns {string} the serialized element.
	 */
	function serializeGroupElement(element) {
		checkNotNull(element);
		return HEX_PREFIX + element.value.toString(16).toUpperCase();
	}

	/**
	 * Serializes an ElGamal multi recipient message.
	 *
	 * @param message {ElGamalMultiRecipientMessage}, the message to serialize.
	 * @returns {string} the serialized string.
	 */
	function serializeElGamalMultiRecipientMessage(message) {
		return JSON.stringify(message.stream().map(gqElement => serializeGroupElement(gqElement)));
	}

	/**
	 * Serializes an ElGamal multi recipient ciphertext.
	 *
	 * @param ciphertext {ElGamalMultiRecipientCiphertext}, the ciphertext to serialize.
	 * @returns {string} the serialized string.
	 */
	function serializeElGamalCiphertext(ciphertext) {
		const serializedPhis = ciphertext.phis.elements
			.map(phi => serializeGroupElement(phi));

		const object = {
			gamma: serializeGroupElement(ciphertext.gamma),
			phis: serializedPhis
		};

		return JSON.stringify(object);
	}

	/**
	 * Serializes an ElGamal multi recipient public key.
	 *
	 * @param publicKey {ElGamalMultiRecipientPublicKey}, the public key to serialize.
	 * @returns {string} the serialized string.
	 */
	function serializeElGamalMultiRecipientPublicKey(publicKey) {
		return JSON.stringify(publicKey.stream().map(gqElement => serializeGroupElement(gqElement)));
	}

	/**
	 * Serializes an exponentiation proof.
	 *
	 * @param proof {ExponentiationProof}, the proof to serialize.
	 * @returns {string} the serialized string.
	 */
	function serializeExponentiationProof(proof) {
		const object = {
			e: serializeGroupElement(proof.e),
			z: serializeGroupElement(proof.z)
		};

		return JSON.stringify(object);
	}

	/**
	 * Serializes a plaintext equality proof.
	 *
	 * @param proof {PlaintextEqualityProof}, the proof to serialize.
	 * @returns {string} the serialized string.
	 */
	function serializePlaintextEqualityProof(proof) {
		const serializedZ = proof.z.elements
			.map(el => serializeGroupElement(el));

		const object = {
			e: serializeGroupElement(proof.e),
			z: serializedZ
		};

		return JSON.stringify(object);
	}

	return {
		serializeElGamalCiphertext: serializeElGamalCiphertext,
		serializeExponentiationProof: serializeExponentiationProof,
		serializePlaintextEqualityProof: serializePlaintextEqualityProof,
		serializeElGamalMultiRecipientMessage: serializeElGamalMultiRecipientMessage,
		serializeElGamalMultiRecipientPublicKey: serializeElGamalMultiRecipientPublicKey,
		serializeGroupElement: serializeGroupElement
	}
})();
