/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

module.exports = function (
	ContestService,
	ListService,
	CandidateService,
) {
	'ngInject';

	let _this = {};

	const initialize = contest => {
		// If selectedLists exists it means the contest was previously initialized
		// and now the reinitialization is triggered by "change vote"
		if (contest.selectedLists) {
			return;
		}

		contest.selectedLists = [];

		_.each(contest.lists, list => {
			ListService.mapDetailsToModel(list);
			list.chosen = false;
		});

		// If selectedCandidates exists it means the contest was previously initialized
		// and now the reinitialization is triggered by "change vote"
		if (contest.selectedCandidates) {
			return;
		}

		contest.selectedCandidates = [];

		_.each(contest.lists, list => {
			_.each(list.candidates, candidate => {
				CandidateService.mapDetailsToModel(candidate);
				candidate.chosen = 0;

				// Make sure all candidates have enough representations to be accumulated
				if (
					candidate.allIds &&
					list.parent.candidatesQuestion.cumul &&
					candidate.allIds.length < list.parent.candidatesQuestion.cumul
				) {
					console.warn(
						`Candidate ${candidate.id} has less representations than the maximum allowed cumul.`,
					);
				}
			});

			list.candidatesSummary = ListService.getCandidatesSummary(list);
		});
	};

	const validate = contest => {
		_this._fillEmptiesWithBlanks(contest);

		contest.error = false;
		contest.errors = [];
	};


	const _fillEmptiesWithBlanks = contest => {
		const blankList = ListService.getBlankList(contest);

		if (!blankList) {
			return;
		}

		for (let i = 0; i < contest.listQuestion.maxChoices; i++) {
			if (!contest.selectedLists[i]) {
				blankList.chosen = true;
				contest.selectedLists.push(blankList);
			}
		}

		for (let i = 0; i < contest.candidatesQuestion.maxChoices; i++) {
			if (!contest.selectedCandidates[i]) {
				ContestService.addBlankCandidate(contest, i);
			}
		}
	};

	const clearList = contest => {
		_.each(contest.selectedLists, function (selectedList) {
			if (selectedList) {
				selectedList.chosen = false;
			}
		});

		ContestService.clearCandidates(contest);

		contest.selectedLists.length = 0;
	};

	const hasListSelected = contest => {
		const nonBlankSelectedList = _.find(
			contest.selectedLists,
			selectedList => !selectedList.isBlank,
		);

		return !!nonBlankSelectedList;
	};

	const hasCandidatesSelected = contest => {
		const nonBlankCandidate = _.find(
			contest.selectedCandidates,
			selectedCandidate => !selectedCandidate.isBlank,
		);

		return !!nonBlankCandidate;
	};

	const addSelectedListToContest = list => {
		if (!list) {
			return;
		}

		const contest = list.parent;

		_this.clearList(contest);

		list.chosen = true;
		contest.selectedLists.push(list);

		ListService.addListCandidatesToContest(list);

		return {
			contestId: contest.id,
			listId: list.getQualifiedId(),
		};
	};

	_this = {
		initialize,
		hasListSelected,
		hasCandidatesSelected,
		validate,
		addSelectedListToContest,
		clearList,
		_fillEmptiesWithBlanks,
	};

	return _this;
};
