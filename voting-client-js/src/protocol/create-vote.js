/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {ElGamalMultiRecipientCiphertext} = require("crypto-primitives-ts/lib/cjs/elgamal/elgamal_multi_recipient_ciphertext");
const {ElGamalMultiRecipientMessage} = require("crypto-primitives-ts/lib/cjs/elgamal/elgamal_multi_recipient_message");
const {ElGamalMultiRecipientPublicKey} = require("crypto-primitives-ts/lib/cjs/elgamal/elgamal_multi_recipient_public_key");
const {GroupVector} = require("crypto-primitives-ts/lib/cjs/group_vector");
const {GqElement} = require("crypto-primitives-ts/lib/cjs/math/gq_element");
const {ImmutableBigInteger} = require("crypto-primitives-ts/lib/cjs/immutable_big_integer");
const {RandomService} = require("crypto-primitives-ts/lib/cjs/math/random_service");
const {ZeroKnowledgeProofService} = require("crypto-primitives-ts/lib/cjs/zeroknowledgeproofs/zero_knowledge_proof_service");
const {ZqElement} = require("crypto-primitives-ts/lib/cjs/math/zq_element");
const {ZqGroup} = require("crypto-primitives-ts/lib/cjs/math/zq_group");
const {checkArgument, checkNotNull} = require("crypto-primitives-ts/lib/cjs/validation/preconditions");
const {integerToString} = require("crypto-primitives-ts/lib/cjs/conversions");

module.exports = (function () {
	'use strict';

	// Maximum value of selectable voting options
	const phi = 120;

	/**
	 * Implements the CreateVote algorithm described in the cryptographic protocol.
	 *
	 * @param {string} verificationCardId, vc_id the verification card id
	 * @param {number[]} votersSelections, s_id the voter's selections
	 * @param {ElGamalMultiRecipientPublicKey} electionPublicKey, EL_pk the election public key
	 * @param {ZqElement} verificationCardSecretKey, k_id the verification card secret key
	 * @param {string} electionEventId, ee the election event id
	 * @returns {Vote}, the vote object.
	 */
	function createVote(
		verificationCardId,
		votersSelections,
		electionPublicKey,
		verificationCardSecretKey,
		electionEventId
	) {
		const vc_id = checkNotNull(verificationCardId);
		const s_id = checkNotNull(votersSelections);
		const EL_pk = checkNotNull(electionPublicKey);
		const k_id = checkNotNull(verificationCardSecretKey);
		const ee = checkNotNull(electionEventId);

		const randomService = new RandomService();
		const zeroKnowledgeProofService = new ZeroKnowledgeProofService();
		const gqGroup = EL_pk.group;
		const q = gqGroup.q;
		const psi = s_id.length;

		const identityElement = GqElement.fromValue(ImmutableBigInteger.ONE, gqGroup)
		const pk_CCR = new ElGamalMultiRecipientPublicKey(s_id.map(() => identityElement));

		// Input cross validations
		checkArgument(EL_pk.group.equals(pk_CCR.group), "The election public key and the choice return codes encryption public key" +
			" must have the same group");
		checkArgument(EL_pk.group.hasSameOrderAs(k_id.group), "The election public key and the verification card secret key must have same group order.");

		// Ensures
		checkArgument(new Set(s_id).size === s_id.length, "The voter cannot select the same option twice.");
		checkArgument(0 < psi && psi <= phi, "The number of selectable voting options must be greater than zero" +
			" and less than or equal to the maximum value of voting options");

		// Operations
		const encodedVotingOptions = s_id.map(prime => {
			const immutablePrime = ImmutableBigInteger.fromNumber(prime);
			return GqElement.fromValue(immutablePrime, gqGroup);
		});

		const identity = gqGroup.identity;
		const reducer = (product, currentValue) => product.multiply(currentValue);
		const rho_element = encodedVotingOptions.reduce(reducer, identity);
		const rho = new ElGamalMultiRecipientMessage([rho_element]);

		const r_value = randomService.genRandomInteger(q);
		const r = ZqElement.create(r_value, ZqGroup.sameOrderAs(gqGroup));

		const E1 = ElGamalMultiRecipientCiphertext.getCiphertext(rho, r, new ElGamalMultiRecipientPublicKey([EL_pk.get(0)]));

		const pCC_id_elements = [];
		for (let i = 0; i < psi; i++) {
			pCC_id_elements[i] = encodedVotingOptions[i].exponentiate(k_id);
		}
		const pCC_id = new ElGamalMultiRecipientMessage(pCC_id_elements);

		const r_prime_value = randomService.genRandomInteger(q);
		const r_prime = ZqElement.create(r_prime_value, ZqGroup.sameOrderAs(gqGroup));

		const E2 = ElGamalMultiRecipientCiphertext.getCiphertext(pCC_id, r_prime, pk_CCR);

		const E1_tilde = E1.exponentiate(k_id);

		const E2_tilde_phis = E2.phis.elements.reduce(reducer, identity);
		const E2_tilde = ElGamalMultiRecipientCiphertext.create(E2.gamma, [E2_tilde_phis]);

		const K_id = gqGroup.generator.exponentiate(k_id);

		const EL_pk_toString = EL_pk.stream().map(element => integerToString(element.value));
		const i_aux = [ee, vc_id, ...EL_pk_toString, "CreateVote"];

		const bases = GroupVector.of(gqGroup.generator, E1.gamma, E1.get(0));
		const exponentiations = GroupVector.of(K_id, E1_tilde.gamma, E1_tilde.get(0))
		const pi_exp = zeroKnowledgeProofService.genExponentiationProof(bases, k_id, exponentiations, i_aux);

		const pk_CCR_tilde = pk_CCR.stream().slice(0, psi).reduce(reducer, identity);

		const randomness = GroupVector.of(r.multiply(k_id), r_prime);
		const pi_EqEnc = zeroKnowledgeProofService.genPlaintextEqualityProof(E1_tilde, E2_tilde, EL_pk.get(0), pk_CCR_tilde, randomness, i_aux);

		/**
		 * @typedef {object} Vote
		 * @property {ElGamalMultiRecipientCiphertext} encryptedVote, E1 The encrypted vote
		 * @property {ElGamalMultiRecipientCiphertext} encryptedPartialChoiceReturnCodes, E2 The encrypted partial Choice Return Codes
		 * @property {ElGamalMultiRecipientCiphertext} exponentiatedEncryptedVote, E1_tilde The exponentiated encrypted vote
		 * @property {ExponentiationProof} exponentiationProof, pi_exp The exponentiation proof
		 * @property {PlaintextEqualityProof} plaintextEqualityProof, pi_EqEnc The plaintext equality proof
		 */
		return {
			encryptedVote: E1,
			encryptedPartialChoiceReturnCodes: E2,
			exponentiatedEncryptedVote: E1_tilde,
			exponentiationProof: pi_exp,
			plaintextEqualityProof: pi_EqEnc
		};
	}

	return createVote;
})();
