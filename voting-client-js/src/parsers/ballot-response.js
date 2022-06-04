/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */
/* jshint maxlen: 666  */

module.exports = (function () {
	'use strict';

	// validate ballot response
	// returns: {OV.Ballot}, the ballot

	const validateBallotResponse = function (response) {
		const ballot = OV.BallotParser.parseBallot(response.ballot);
		OV.BallotParser.parseTexts(ballot, response.ballotTexts);

		return ballot;
	};

	return validateBallotResponse;
})();
