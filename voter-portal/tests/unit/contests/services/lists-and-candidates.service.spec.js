/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

describe('Lists And Candidates Service', () => {

	let ContestService;
	let ListsAndCandidatesService;
	let ListService;
	let CandidateService;
	let contest;

	const contestInitialState = require('../mocks/lists-and-candidates.json');
	const {
		addParentsReferences,
	} = require('../util.js');

	beforeEach(() => {

		angular.mock.module('app.services');
		angular.mock.module('app.contests');

	});

	beforeEach(inject(
		(_ContestService_, _ListsAndCandidatesService_, _ListService_, _CandidateService_) => {

			ContestService = _ContestService_;
			ListsAndCandidatesService = _ListsAndCandidatesService_;
			ListService = _ListService_;
			CandidateService = _CandidateService_;

			contest = angular.copy(contestInitialState);
			addParentsReferences(contest);

		})
	);

	it('should be defined', () => {

		expect(ListsAndCandidatesService).toBeDefined();

	});

	describe('initialize()', () => {

		beforeEach(() => {

			spyOn(ListService, 'mapDetailsToModel');
			spyOn(CandidateService, 'mapDetailsToModel');

		});

		it('initializes the contest', () => {

			expect(contest.selectedLists).toBeUndefined();
			expect(contest.selectedCandidates).toBeUndefined();

			ListsAndCandidatesService.initialize(contest);

			expect(contest.selectedLists.length).toBe(0);
			expect(contest.selectedCandidates.length).toBe(0);

			for (let i = 0; i < contest.lists.length; i++) {

				expect(ListService.mapDetailsToModel)
					.toHaveBeenCalledWith(contest.lists[i]);

				expect(contest.lists[i].chosen).toBe(false);

				for (let j = 0; j < contest.lists[i].candidates.length; j++) {

					expect(CandidateService.mapDetailsToModel)
						.toHaveBeenCalledWith(contest.lists[i].candidates[j]);
					expect(contest.lists[i].candidates[j].chosen).toBe(0);

				}

			}

		});

		it('warns if a candidate\'s cumul is grater than the number of representations he has', () => {

			spyOn(console, 'warn');

			contest.candidatesQuestion.cumul = 2;
			contest.lists[1].parent = contest;
			contest.lists[1].candidates[0].allIds = [1];

			ListsAndCandidatesService.initialize(contest);

			expect(console.warn).toHaveBeenCalled();
			expect(console.warn.calls.argsFor(0)[0]).toContain(contest.lists[1].candidates[0].id);

		});


		it('does not reinitialize the displayedLists if it already exists (change vote scenario)', () => {

			contest.selectedLists = [1];
			contest.selectedCandidates = [2];

			ListsAndCandidatesService.initialize(contest);

			expect(contest.selectedLists).toEqual([1]);

			expect(ListService.mapDetailsToModel).not.toHaveBeenCalled();

		});

		it('does not reinitialize the selectedCandidates if it already exists (change vote scenario)', () => {

			contest.selectedCandidates = [2];

			ListsAndCandidatesService.initialize(contest);

			expect(contest.selectedCandidates).toEqual([2]);

			expect(CandidateService.mapDetailsToModel).not.toHaveBeenCalled();

		});


	});

	describe('addSelectedListToContest()', () => {

		let blueList;
		let greenList;
		let getMaxAvailableSeatsSpy;
		let clearListSpy;

		beforeEach(() => {

			ListsAndCandidatesService.initialize(contest);

			blueList = contest.lists.filter(list => list.name === 'Blue List')[0];
			greenList = contest.lists.filter(list => list.name === 'Green List')[0];

			getMaxAvailableSeatsSpy = spyOn(ListService, 'getMaxAvailableSeats');
			clearListSpy = spyOn(ListsAndCandidatesService, 'clearList');

			getMaxAvailableSeatsSpy.and.returnValue(10);

		});

		xit('selects a list', () => {

			expect(contest.selectedLists.length).toBe(0);

			ListsAndCandidatesService.addSelectedListToContest(blueList);

			expect(clearListSpy).toHaveBeenCalledWith(greenList.parent);

			expect(contest.selectedLists).toContain(blueList);
			expect(contest.selectedCandidates).toEqual(blueList.candidates);

			expect(contest.selectedCandidates
				.filter(candidate => candidate.isBlank === true).length).toBe(0);


		});

		it('marks list as chosen when selected', () => {

			expect(blueList.chosen).toBe(false);

			ListsAndCandidatesService.addSelectedListToContest(blueList);

			expect(blueList.chosen).toBe(true);

			ListsAndCandidatesService.addSelectedListToContest(greenList);

			expect(greenList.chosen).toBe(true);

		});

		xit('selects first N candidates of a list if the number of candidates is greater than the number of slots', () => {

			getMaxAvailableSeatsSpy.and.returnValue(1);

			ListsAndCandidatesService.addSelectedListToContest(blueList);

			expect(contest.selectedCandidates).toEqual([
				blueList.candidates[0],
			]);

		});

	});

	describe('clearList()', () => {

		let blueList;

		beforeEach(() => {

			ListsAndCandidatesService.initialize(contest);

			blueList = contest.lists.filter(list => list.name === 'Blue List')[0];

			spyOn(ContestService, 'clearCandidates');

		});

		it('clears a selected list', () => {

			blueList.chosen = true;
			contest.selectedLists = [blueList];

			ListsAndCandidatesService.clearList(contest);

			expect(contest.selectedLists.length).toEqual(0);
			expect(ContestService.clearCandidates).toHaveBeenCalledWith(contest);
			expect(blueList.chosen).toBe(false);

		});

	});

	describe('_fillEmptiesWithBlanks()', () => {

		let blankList;
		let addBlankCandidateSpy;

		beforeEach(() => {

			ListsAndCandidatesService.initialize(contest);

			blankList = contest.lists.find(l => l.isBlank);

			spyOn(ListService, 'getBlankList').and.returnValue(blankList);
			addBlankCandidateSpy = spyOn(ContestService, 'addBlankCandidate');

		});

		it('fills empty spots with blank representations (all empty)', () => {

			ListsAndCandidatesService._fillEmptiesWithBlanks(contest);

			expect(contest.selectedLists).toEqual([blankList]);
			expect(addBlankCandidateSpy.calls.count()).toBe(contest.candidatesQuestion.maxChoices);


		});

		it('does not fill with blanks as no empty spots', () => {

			contest.candidatesQuestion.maxChoices = 3;

			contest.selectedLists = [1];
			contest.selectedCandidates = [1, 2, 3];

			ListsAndCandidatesService._fillEmptiesWithBlanks(contest);

			expect(contest.selectedLists).toEqual([1]);
			expect(addBlankCandidateSpy.calls.count()).toBe(0);

		});

		it('calls ContestService.addBlankCandidate for the empty spots', () => {

			contest.candidatesQuestion.maxChoices = 3;

			contest.selectedCandidates = [1, 2, 3];

			delete contest.selectedCandidates[0];
			delete contest.selectedCandidates[2];

			ListsAndCandidatesService._fillEmptiesWithBlanks(contest);

			expect(addBlankCandidateSpy.calls.count()).toBe(2);
			expect(addBlankCandidateSpy.calls.argsFor(0)[1]).toBe(0);
			expect(addBlankCandidateSpy.calls.argsFor(1)[1]).toBe(2);

		});

		it('should fill with blanks the empty spots', () => {

			contest.candidatesQuestion.maxChoices = 3;

			contest.selectedCandidates = [1, 2, 3];

			delete contest.selectedCandidates[0];
			delete contest.selectedCandidates[2];

			ListsAndCandidatesService._fillEmptiesWithBlanks(contest);

			expect(blankList.chosen).toBe(true);
			expect(contest.selectedLists).toEqual([blankList]);
			expect(addBlankCandidateSpy.calls.count()).toBe(2);
			expect(addBlankCandidateSpy.calls.argsFor(0)[1]).toBe(0);
			expect(addBlankCandidateSpy.calls.argsFor(1)[1]).toBe(2);

		});

	});

	describe('validate()', () => {

		let fillEmptiesWithBlanksSpy;

		beforeEach(() => {

			ListsAndCandidatesService.initialize(contest);

			fillEmptiesWithBlanksSpy = spyOn(ListsAndCandidatesService, '_fillEmptiesWithBlanks');

		});

		it('should fill empty slots with blanks', () => {

			ListsAndCandidatesService.validate(contest);

			expect(fillEmptiesWithBlanksSpy).toHaveBeenCalledWith(contest);

		});

	});

});
