/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* jshint maxlen: 666  */

module.exports = (function () {
	'use strict';

	// validate vote cast response
	// returns: vote cast code
	return function (
		response,
		trustedVotingCardId,
		trustedElectionEventId,
		trustedVerificationCardId,
	) {
		const electionEventId = response.electionEventId;
		const votingCardId = response.votingCardId;
		const verificationCardId = response.verificationCardId;

		if (
			electionEventId !== trustedElectionEventId ||
			votingCardId !== trustedVotingCardId ||
			verificationCardId !== trustedVerificationCardId
		) {
			throw new Error('Bad vote');
		}

		const voteCastMessage = response.voteCastMessage;

		return {
			voteCastCode: voteCastMessage.voteCastCode
		};
	};
})();
