/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

module.exports = function () {
	'ngInject';

	return {
		mapDetailsToModel: candidate => {
			if (!candidate || !candidate.details) {
				console.warn('Passed candidate has no details to be mapped.');

				return;
			}

			const nameKey = candidate.isBlank ? 'text' : 'candidateType_attribute1';
			const attr2Key = 'candidateType_attribute2';
			const attr3Key = 'candidateType_attribute3';
			const positionInListKey = 'candidateType_positionOnList';

			const {
				[nameKey]: name, // Translates to: const name = candidate.details[nameKey]
				[attr2Key]: attr2,
				[attr3Key]: attr3,
				[positionInListKey]: order,
			} = candidate.details;

			//  Prevent overwriting of a writeIn's existing name
			if (!candidate.isWriteIn || !candidate.name) {
				candidate.name = name;
			}

			// The bio is displayed in the expendable
			// panel of the candidate. Each element of the
			// bio array will be displayed on a row
			candidate.bio = attr2 ? [attr2] : null;

			if (order && order.length) {
				candidate.order = _.map(order.split(','), o => Number(o) - 1);
			}

			// used as "incumbent"; displayed as a separate
			// column next to the candidate's name
			candidate.attribute3 = attr3;
		},

		getAllCandidates: function (contest) {
			const candidates = [];

			if (!contest || !contest.lists || !contest.lists.length) {
				return [];
			}

			_.each(contest.lists, function (list) {
				if (!list.isBlank) {
					_.each(list.candidates, function (candidate) {
						if (!candidate.isBlank) {
							candidates.push(candidate);
						}
					});
				}
			});

			return candidates;
		},

		isWriteInValid: function (candidate) {
			if (!candidate || !candidate.isWriteIn) {
				return true;
			}

			try {
				// If name consists of only spaces, make it invalid
				if (!candidate.name.trim()) {
					return 'name';
				}

				const ballot = candidate.parent.parent.parent;
				const validAlphabet = ballot.writeInAlphabet.substring(1, ballot.writeInAlphabet.length);
				const validAlphabetRegex = new RegExp(`^[${validAlphabet}]+$`);

				// If all chars of the name are found in the array, then is valid
				const isUsingValidAlphabet = validAlphabetRegex.test(candidate.name);

				if (!isUsingValidAlphabet) {
					return 'alphabet';
				}

				const validNameRegex = new RegExp('(?:[^\\s]+)(?:\\s+[^\\s]+)');
				const isValidName = validNameRegex.test(candidate.name);

				if (!isValidName) {
					return 'name';
				}

				return true;
			} catch (ex) {
				console.warn('Candidate writein was not validated due to an error.');

				return true;
			}
		},

		getCandidatePosition: function (contest, candidate) {
			return _.findIndex(contest.selectedCandidates, o => {
				return o && o.id === candidate.id;
			});
		},

		isCandidateSelected: function (candidate) {
			if (!candidate || typeof candidate.chosen !== 'number') {
				return false;
			}

			return candidate.chosen > 0;
		},

		getCandidateCumul: function (candidate) {
			if (!candidate || !candidate.id) {
				return 0;
			}

			const list = candidate.parent;
			const contest = list.parent;

			if (!contest.selectedCandidates || !contest.selectedCandidates.length) {
				return 0;
			}

			const candidateCumul = _.filter(contest.selectedCandidates, function (c) {
				return c && c.id === candidate.id;
			});

			return candidateCumul.length;
		},

		// All candidates will come with the required amount of representations
		// that would allow them to be accumulated. E.g. If no accumulation allowed
		// candidate will have only one representation. If candidate accumulation is set
		// to 3, then 3 representations should be found for the same candidate.
		// If candidate has initial accumulation, the representations will still be equal
		// to the maximum allowed accumulation (or initial accumulation in case no
		// accumulation is allowed), representations which will be shared among the
		// clones created to be used when searching and selecting a candidate.
		// So if candidates accumulation is 3 and the candidate will have initial accumulation
		// of 2, then the first clone of the candidate will have one representation
		// and the second one will have the rest of 2. If initial accumulation is 2, and no
		// accumulation is allowed, then the 2 resulted clones will have one representation each.
		candidateMaxAllowedCumul: function (candidate) {
			if (
				!candidate ||
				!candidate.allRepresentations ||
				!candidate.allRepresentations.length
			) {
				return 1;
			}

			return candidate.allRepresentations.length || 1;
		},

		isAliasSelected: function (candidate) {
			if (!candidate || typeof candidate.alias !== 'string') {
				return false;
			}

			const list = candidate.parent;
			const contest = list.parent;

			if (
				!contest.candidatesQuestion.fusions ||
				!contest.candidatesQuestion.fusions.length
			) {
				return false;
			}

			const aliasCandidate = contest.selectedCandidates.find(c => {
				if (c && c.id !== candidate.id) {
					const currentSelectedAlias = c.alias;
					const possibleAllianceAlias = candidate.alias;

					const isSameCandidate = contest.candidatesQuestion.fusions.find(
						function (o) {
							return (
								o.indexOf(currentSelectedAlias) > -1 &&
								o.indexOf(possibleAllianceAlias) > -1
							);
						},
					);

					if (isSameCandidate) {
						return true;
					}
				}

				return false;
			});

			return !!aliasCandidate;
		},
	};
};
