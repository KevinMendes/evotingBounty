/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

module.exports = function (
	sessionService,
	contestTypes,
	ListService,
	CandidateService,
) {
	'ngInject';

	let _this = {};

	/**
	 * Get the contest's template type
	 */
	const isType = function (type) {
		// template CANDIDATES_ONLY not sent by server
		if (type === contestTypes.CANDIDATES_ONLY) {
			return (
				this.template === contestTypes.LISTS_AND_CANDIDATES &&
				(!this.listQuestion || this.listQuestion.maxChoices === 0)
			);
		}

		if (type === contestTypes.LISTS_AND_CANDIDATES) {
			return (
				this.template === contestTypes.LISTS_AND_CANDIDATES &&
				this.listQuestion &&
				this.listQuestion.maxChoices > 0
			);
		}

		return this.template === type;
	};

	const canCumulate = function () {
		return (
			this.hasOwnProperty('candidatesQuestion') &&
			this.candidatesQuestion.cumul > 1
		);
	};

	const mapContestDetails = contest => {
		if (contest && contest.lists && contest.lists.length) {
			contest.lists.forEach(list => {
				ListService.mapDetailsToModel(list);

				list.candidates.forEach(candidate => {
					CandidateService.mapDetailsToModel(candidate);
				});
			});
		}
	};

	const selectCandidate = function (candidate, position) {
		if (_.isEmpty(candidate) || typeof position !== 'number' || position < 0) {
			return false;
		}

		const list = candidate.parent;
		const contest = list.parent;

		// remove old candidate
		const oldCandidate = contest.selectedCandidates[position];

		if (oldCandidate) {
			oldCandidate.chosen--;
		}

		// add new one
		candidate.chosen++;

		contest.selectedCandidates[position] = candidate;
	};

	const getSelectedCandidatePositions = candidate => {
		if (_.isEmpty(candidate)) {
			return [];
		}

		const list = candidate.parent;
		const contest = list.parent;

		const positions = [];

		for (const position in contest.selectedCandidates) {
			if (candidate === contest.selectedCandidates[position]) {
				positions.push(parseInt(position));
			}
		}

		return positions;
	};

	/**
	 * Returns the index of the first empty slot or -1 if none was found
	 */
	const findEmptyPosition = contest => {
		if (_.isEmpty(contest)) {
			return -1;
		}

		for (let i = 0; i < contest.candidatesQuestion.maxChoices; i++) {
			if (
				!contest.selectedCandidates[i] ||
				contest.selectedCandidates[i].isBlank
			) {
				return i;
			}
		}

		return -1;
	};

	const addBlankCandidate = (contest, position) => {
		if (position === -1) {
			return;
		}

		const blankCandidate = _this._getAvailableBlankCandidate(contest, position);

		if (blankCandidate) {
			_this.selectCandidate(blankCandidate, position);
		}
	};

	const addWriteInCandidate = (contest, position, desiredName) => {
		if (position === -1) {
			return;
		}

		const writeInCandidate = _this._getAvailableWriteInCandidate(
			contest,
			position,
		);

		if (writeInCandidate) {
			writeInCandidate.name = desiredName;

			_this.selectCandidate(writeInCandidate, position);
		}
	};

	const clearCandidate = function (candidate, position) {
		if (_.isEmpty(candidate) || candidate.chosen < 1) {
			return angular.noop();
		}

		candidate.chosen--;

		// Make sure the name of a write-in is emptied on clear
		if (candidate.isWriteIn) {
			candidate.name = '';
		}

		const list = candidate.parent;
		const contest = list.parent;

		delete contest.selectedCandidates[position];
	};

	const clearCandidates = contest => {
		_.each(contest.selectedCandidates, function (selectedCandidate, position) {
			if (selectedCandidate) {
				_this.clearCandidate(selectedCandidate, position);
			}
		});

		contest.selectedCandidates.length = 0;
	};

	const _assembleOptionsVote = function (contest) {
		return contest.questions
			.map(question => {
				let selectedOption;

				if (question.chosen) {
					selectedOption = question.options.find(o => o.id === question.chosen);
				}

				if (!selectedOption && question.blankOption) {
					selectedOption = question.blankOption;
				}

				if (!selectedOption) {
					return null;
				}

				return {
					id: question.id,
					prime: selectedOption.prime || selectedOption.representation,
				};
			})
			.filter(o => !!o);
	};

	const _mapListsAsVoteOptions = contest => {
		if (!contest.selectedLists || !contest.selectedLists.length) {
			return [];
		}

		return contest.selectedLists.map(list => {
			return {
				id: list.id,
				prime: list.prime,
			};
		});
	};

	// Due to cumul, an option can be selected multiple times, but for each
	// same selection, a separate id and prime has to be used
	const _getUnusedRepresentationOfOption = (selectedOptions, option) => {
		if (!option.allIds || !option.allIds.length) {
			return {
				id: option.id,
				prime: option.prime,
			};
		}

		for (let i = 0; i < option.allIds.length; i++) {
			const usedRepresentation = selectedOptions.find(
				o => o.id === option.allIds[i],
			);

			if (!usedRepresentation) {
				return {
					id: option.allIds[i],
					prime: option.allRepresentations[i],
				};
			}
		}

		console.warn(
			`No unused representation have been found for option ${option.id} (attribute: ${option.attribute})`,
		);

		return null;
	};

	const _mapCandidatesAsVoteOptions = contest => {
		if (!contest.selectedCandidates || !contest.selectedCandidates.length) {
			return [];
		}

		const uniqueRepresentations = [];

		contest.selectedCandidates.forEach(candidate => {
			uniqueRepresentations.push(
				_getUnusedRepresentationOfOption(uniqueRepresentations, candidate),
			);
		});

		return uniqueRepresentations;
	};

	const _assembleListsAndCandidatesVote = function (contest) {
		return _.concat(
			_mapListsAsVoteOptions(contest),
			_mapCandidatesAsVoteOptions(contest),
		);
	};

	const _assembleWriteIns = function (contest) {
		let writeInsSeparator = '#';

		if (contest.parent && contest.parent.writeInAlphabet) {
			writeInsSeparator = contest.parent.writeInAlphabet[0];
		}

		return _.filter(contest.selectedCandidates, {isWriteIn: true}).map(
			candidate => {
				return `${candidate.prime}${writeInsSeparator}${candidate.name}`;
			},
		);
	};

	const extractVotedOptionsRepresentations = contest => {
		let voteOptions = [];
		let writeIns = [];
		const isContestType = isType.bind(contest);

		if (isContestType(contestTypes.OPTIONS)) {
			voteOptions = _assembleOptionsVote(contest);
		} else if (
			isContestType(contestTypes.LISTS_AND_CANDIDATES) ||
			isContestType(contestTypes.CANDIDATES_ONLY)
		) {
			voteOptions = _assembleListsAndCandidatesVote(contest);
			writeIns = _.concat(writeIns, _assembleWriteIns(contest));
		} else {
			console.error('Undefined contest type');

			return null;
		}

		return {
			voteOptions,
			writeIns,
		};
	};

	const getPrimes = function (voteOptions) {
		if (!voteOptions || _.isEmpty(voteOptions)) {
			return [];
		}

		return voteOptions
			.map(o => {
				return o.prime || null;
			})
			.filter(o => !!o);
	};

	const isCompletelyBlank = contest => {
		// Options Contest
		if (contest.questions) {
			for (const question of contest.questions) {
				const chosenOption = question.options.find(
					o => o.id === question.chosen,
				);

				if (chosenOption && !chosenOption.isBlank) {
					return false;
				}
			}
		}

		// List and Candidates Contest
		if (contest.selectedLists) {
			const nonBlankList = _.find(contest.selectedLists, {
				isBlank: false,
			});

			if (nonBlankList) {
				return false;
			}
		}

		// List and Candidates or Candidates Only Contests
		if (contest.selectedCandidates) {
			const nonBlankCandidate = _.find(contest.selectedCandidates, {
				isBlank: false,
			});

			if (nonBlankCandidate) {
				return false;
			}
		}

		return true;
	};

	const hasAllSeatsOccupied = contest => {
		// List and Candidates or Candidates Only Contests
		if (!contest || !contest.selectedCandidates) {
			return false;
		}

		const selectedNonBlankCandidates = _.filter(contest.selectedCandidates, {
			isBlank: false,
		});

		return (
			selectedNonBlankCandidates.length ===
			contest.candidatesQuestion.maxChoices
		);
	};

	/**
	 * Private methods (some are exposed for unit testing)
	 */

	const _getAvailableBlankCandidate = (contest, position) => {
		const blankListCandidates = _this._getListCandidates(
			ListService.getBlankList(contest),
		);

		const blankCandidates = blankListCandidates.filter(c => c.isBlank);

		// Specific swiss-post requirement
		if (contest.candidatesQuestion.maxChoices === blankCandidates.length) {
			return blankCandidates[position];
		}

		return _.find(blankCandidates, {
			chosen: 0,
			isBlank: true,
		});
	};

	// clear writeIn candidates name when not chosen
	const _clearNotChosenWriteIns = candidates => {
		candidates.forEach(candidate => {
			if (!candidate.chosen) {
				candidate.name = undefined;
			}
		});

		return candidates;
	};

	const _getAvailableWriteInCandidate = (contest, position) => {
		const blankListCandidates = _this._getListCandidates(
			ListService.getBlankList(contest),
		);

		const writeInsCandidates = _this._clearNotChosenWriteIns(
			blankListCandidates.filter(c => c.isWriteIn),
		);

		// Specific swiss-post requirement
		if (contest.candidatesQuestion.maxChoices === writeInsCandidates.length) {
			return writeInsCandidates[position];
		}

		return _.find(writeInsCandidates, {
			chosen: 0,
			isWriteIn: true,
		});
	};

	const _getListCandidates = list => {
		if (!list || !list.candidates || !list.candidates.length) {
			return [];
		}

		return list.candidates;
	};

	_this = {
		isType,
		canCumulate,
		mapContestDetails,
		selectCandidate,
		getSelectedCandidatePositions,
		addBlankCandidate,
		addWriteInCandidate,
		clearCandidates,
		clearCandidate,
		extractVotedOptionsRepresentations,
		getPrimes,
		isCompletelyBlank,
		findEmptyPosition,
		hasAllSeatsOccupied,
		_getAvailableBlankCandidate,
		_getAvailableWriteInCandidate,
		_clearNotChosenWriteIns,
		_getListCandidates,
	};

	return _this;
};
