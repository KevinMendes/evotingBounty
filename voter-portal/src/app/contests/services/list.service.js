/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

module.exports = function () {
	'ngInject';

	return {
		mapDetailsToModel: function (list) {
			if (!list || !list.details) {
				console.warn('Passed list has no details to be mapped.');

				return;
			}

			const nameKey = list.isBlank ? 'text' : 'listType_attribute1';
			const description1Key = 'listType_attribute2';
			const description2Key = 'listType_attribute3';

			const {
				[nameKey]: name, // Translates to: const name = list.details[nameKey]
				[description1Key]: description1,
				[description2Key]: description2,
			} = list.details;

			list.name = name;
			list.description = [description1, description2]
				.filter(o => o && o.trim())
				.map(o => o.trim());
		},

		getAllLists: function (contest) {
			return _.filter(contest.lists, list => {
				return !list.isBlank && !list.isWriteIn;
			});
		},

		getBlankList: function (contest) {
			return _.find(contest.lists, {
				isBlank: true,
			});
		},

		getMaxAvailableSeats: function (contest) {
			if (
				contest &&
				contest.candidatesQuestion &&
				typeof contest.candidatesQuestion.maxChoices === 'number'
			) {
				return contest.candidatesQuestion.maxChoices;
			}

			console.error(
				'Contest has no candidates question or max choices is invalid.',
			);

			return 0;
		},

		hasListDetails: function (list) {
			try {
				return (
					!!list &&
					list.hasOwnProperty('description') &&
					list.description.length > 0
				);
			} catch (ex) {
				return false;
			}
		},

		/**
		 * Adds all the candidates of a list to a contest taking into account
		 * the initial accumulation configured for each of it. If a candidate
		 * has initial cumul equal to 3, he'll be added to the contest 3 times
		 * (if the number of seats allows it)
		 */
		addListCandidatesToContest: function (list) {
			const contest = list.parent;
			const seats = this.getMaxAvailableSeats(contest);
			const addCandidateToContest = (candidate, position) => {
				// since selectedCandidates array can have empty positions
				// check the length only for the non empty ones
				if (
					contest.selectedCandidates.filter(hasValue => hasValue).length < seats
				) {
					if (!isNaN(position) && position > seats) {
						return;
					}

					candidate.chosen++;

					if (position >= 0) {
						contest.selectedCandidates[position] = candidate;
					} else {
						contest.selectedCandidates.push(candidate);
					}
				}
			};

			_.each(list.candidates, function (candidate) {
				// If candidate has initial cumul, try to add him up to the number
				// equal to initialAccumulation (if seats available)
				if (candidate.details.candidateType_initialAccumulation > 1) {
					for (
						let i = 0;
						i < candidate.details.candidateType_initialAccumulation;
						i++
					) {
						const candidateOrder =
							candidate.order && candidate.order.length
								? candidate.order[i]
								: null;

						addCandidateToContest(candidate, candidateOrder);
					}
				} else {
					const candidateOrder =
						candidate.order && candidate.order.length
							? candidate.order[0]
							: null;

					addCandidateToContest(candidate, candidateOrder);
				}
			});
		},

		/**
		 * Returns a list of candidates names of a list, including copies of the
		 * ones with initial cumul. The purpose of this function is to be used
		 * for displaying the candidates by list-selector directive
		 */
		getCandidatesSummary: function (list) {
			const candidates = [];

			_.each(list.candidates, function (candidate) {
				// If candidate has initial cumul, try to add him up to the number
				// equal to initialAccumulation (if seats available)
				if (candidate.details.candidateType_initialAccumulation > 1) {
					for (
						let i = 0;
						i < candidate.details.candidateType_initialAccumulation;
						i++
					) {
						if (candidate.order) {
							candidates[candidate.order[i]] = candidate.name;
						} else {
							candidates.push(candidate.name);
						}
					}
				} else {
					if (candidate.order) {
						candidates[candidate.order[0]] = candidate.name;
					} else {
						candidates.push(candidate.name);
					}
				}
			});

			return candidates;
		},
	};
};
