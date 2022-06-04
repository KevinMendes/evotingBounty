/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

describe('Candidates Only Service', () => {

	let ContestService;
	let CandidatesOnlyService;
	let ListService;
	let CandidateService;
	let contest;

	const contestInitialState = require('../mocks/candidates-only-1.json');
	const {
		addParentsReferences,
	} = require('../util.js');

	beforeEach(() => {

		angular.mock.module('app.services');
		angular.mock.module('app.contests');

	});

	beforeEach(inject(
		(_ContestService_, _CandidatesOnlyService_, _ListService_, _CandidateService_) => {

			ContestService = _ContestService_;
			CandidatesOnlyService = _CandidatesOnlyService_;
			ListService = _ListService_;
			CandidateService = _CandidateService_;

			contest = angular.copy(contestInitialState);
			addParentsReferences(contest);

		})
	);

	it('should be defined', () => {

		expect(CandidatesOnlyService).toBeDefined();

	});

	describe('initialize()', () => {

		it('initializes the contest', () => {

			spyOn(ListService, 'mapDetailsToModel');
			spyOn(CandidateService, 'mapDetailsToModel');

			expect(contest.selectedLists).toBeUndefined();
			expect(contest.selectedCandidates).toBeUndefined();

			CandidatesOnlyService.initialize(contest);

			expect(contest.selectedLists).toBeUndefined();
			expect(contest.selectedCandidates.length).toBe(0);

			for (let i = 0; i < contest.lists.length; i++) {

				expect(ListService.mapDetailsToModel).toHaveBeenCalledWith(contest.lists[i]);

				for (let j = 0; j < contest.lists[i].candidates.length; j++) {

					expect(CandidateService.mapDetailsToModel)
						.toHaveBeenCalledWith(contest.lists[i].candidates[j]);
					expect(contest.lists[i].candidates[j].chosen).toBe(0);

				}

			}

		});

		it('does not reinitialize the displayedLists if it already exists (change vote scenario)', () => {

			spyOn(ListService, 'mapDetailsToModel');

			contest.displayedLists = [1];
			contest.selectedCandidates = [2];

			CandidatesOnlyService.initialize(contest);

			expect(contest.displayedLists).toEqual([1]);

			expect(ListService.mapDetailsToModel).not.toHaveBeenCalled();

		});

		it('does not reinitialize the selectedCandidates if it already exists (change vote scenario)', () => {

			spyOn(ListService, 'mapDetailsToModel');
			spyOn(CandidateService, 'mapDetailsToModel');

			contest.selectedCandidates = [2];

			CandidatesOnlyService.initialize(contest);

			expect(contest.selectedCandidates).toEqual([2]);

			expect(CandidateService.mapDetailsToModel).not.toHaveBeenCalled();

		});

	});

	describe('addSelectedListToContest()', () => {

		let blueList;
		let greenList;

		let getMaxAvailableSeatsSpy;
		let clearCandidatesSpy;

		beforeEach(() => {

			CandidatesOnlyService.initialize(contest);

			blueList = contest.lists.filter(list => list.name === 'Blue List')[0];
			greenList = contest.lists.filter(list => list.name === 'Green List')[0];
			blueList.parent = contest;
			greenList.parent = contest;

			getMaxAvailableSeatsSpy = spyOn(ListService, 'getMaxAvailableSeats');
			clearCandidatesSpy = spyOn(ContestService, 'clearCandidates');

			getMaxAvailableSeatsSpy.and.returnValue(10);

		});

		it('adds the selected list to the displayedLists property of the contest but not in the selected one', () => {

			getMaxAvailableSeatsSpy.and.returnValue(greenList.candidates.length);

			expect(contest.selectedLists).toBeUndefined();
			expect(contest.selectedCandidates.length).toEqual(0);

			CandidatesOnlyService.addSelectedListToContest(greenList);

			expect(clearCandidatesSpy).toHaveBeenCalledWith(greenList.parent);

			expect(contest.selectedLists).toBeUndefined();
			expect(contest.displayedLists).toContain(greenList);

		});

		xit('selects a list', () => {

			CandidatesOnlyService.addSelectedListToContest(blueList);

			expect(contest.displayedLists).toContain(blueList);
			expect(contest.selectedCandidates).toEqual(blueList.candidates);

			expect(contest.selectedCandidates
				.filter(candidate => candidate.isBlank === true).length).toBe(0);
		});

		it('marks the candidates of the selected list as chosen', () => {

			const createArrayOf = (length, desiredValue) => {

				return Array.apply(null, Array(length)).map(() => desiredValue);

			};

			expect(blueList.candidates.map(c => c.chosen))
				.toEqual(createArrayOf(blueList.candidates.length, 0));

			CandidatesOnlyService.addSelectedListToContest(blueList);

			expect(blueList.candidates.map(c => c.chosen))
				.toEqual(createArrayOf(blueList.candidates.length, 1));

			CandidatesOnlyService.addSelectedListToContest(greenList);

			expect(greenList.candidates.map(c => c.chosen))
				.toEqual(createArrayOf(greenList.candidates.length, 1));

		});


		xit('selects first N candidates of a list if the number of candidates is greater than the number of slots', () => {

			getMaxAvailableSeatsSpy.and.returnValue(1);

			CandidatesOnlyService.addSelectedListToContest(blueList);

			expect(contest.selectedCandidates).toEqual([
				blueList.candidates[0],
			]);

		});

	});

	describe('clearList()', () => {

		let clearCandidatesSpy;

		beforeEach(() => {

			CandidatesOnlyService.initialize(contest);

			clearCandidatesSpy = spyOn(ContestService, 'clearCandidates');

		});

		it('clears the displayed list and the selected candidates', () => {

			CandidatesOnlyService.clearList(contest);

			expect(contest.selectedLists).toBeUndefined();
			expect(contest.displayedLists.length).toEqual(0);
			expect(clearCandidatesSpy).toHaveBeenCalledWith(contest);

		});

	});

	describe('_fillEmptiesWithBlanks()', () => {

		let addBlankCandidateSpy;

		beforeEach(() => {

			CandidatesOnlyService.initialize(contest);

			addBlankCandidateSpy = spyOn(ContestService, 'addBlankCandidate');

		});

		it('fills empty spots with blank representations (all empty)', () => {

			CandidatesOnlyService._fillEmptiesWithBlanks(contest);

			expect(addBlankCandidateSpy.calls.count()).toBe(contest.candidatesQuestion.maxChoices);

		});

		it('does not fill with blanks as no empty spots', () => {

			contest.candidatesQuestion.maxChoices = 3;

			contest.selectedCandidates = [1, 2, 3];

			CandidatesOnlyService._fillEmptiesWithBlanks(contest);

			// No blank candidate is found in the selected candidates
			expect(addBlankCandidateSpy.calls.count()).toBe(0);

		});

		it('calls ContestService.addBlankCandidate for the empty spots', () => {

			contest.candidatesQuestion.maxChoices = 3;

			contest.selectedCandidates = [1, 2, 3];

			delete contest.selectedCandidates[0];
			delete contest.selectedCandidates[2];

			CandidatesOnlyService._fillEmptiesWithBlanks(contest);

			expect(addBlankCandidateSpy.calls.count()).toBe(2);
			expect(addBlankCandidateSpy.calls.argsFor(0)[1]).toBe(0);
			expect(addBlankCandidateSpy.calls.argsFor(1)[1]).toBe(2);

		});

	});

	describe('validate()', () => {

		let fillEmptiesWithBlanksSpy;

		beforeEach(() => {

			CandidatesOnlyService.initialize(contest);

			fillEmptiesWithBlanksSpy = spyOn(CandidatesOnlyService, '_fillEmptiesWithBlanks');

		});

		it('should fill empty slots with blanks', () => {

			CandidatesOnlyService.validate(contest);

			expect(fillEmptiesWithBlanksSpy).toHaveBeenCalledWith(contest);

		});

		it('reports error if no candidate was selected and allowFullBlank is false', () => {

			contest.allowFullBlank = false;

			CandidatesOnlyService.validate(contest);

			expect(contest.errors.length).toBe(1);

		});

	});

});
