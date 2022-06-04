/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

describe('Contest Service', () => {

	let ContestService;
	let ListsAndCandidatesService;
	let ListService;
	let CandidateService;
	let contestTypes;
	let contest;

	const lcContestInitialState = require('../mocks/lists-and-candidates.json');
	const candidatesOnlyContestInitialState = require('../mocks/candidates-only-1.json');
	const optionsContestInitialState = require('../mocks/options.json');

	const {
		addParentsReferences,
	} = require('../util.js');

	beforeEach(() => {

		angular.mock.module('app.services');
		angular.mock.module('app.contests');

	});

	beforeEach(inject(
		(_ContestService_, _ListsAndCandidatesService_, _ListService_, _CandidateService_, _contestTypes_) => {

			ContestService = _ContestService_;
			ListsAndCandidatesService = _ListsAndCandidatesService_;
			ListService = _ListService_;
			CandidateService = _CandidateService_;
			contestTypes = _contestTypes_;

			contest = angular.copy(lcContestInitialState);
			addParentsReferences(contest);
			ListsAndCandidatesService.initialize(contest);

		})
	);

	it('should be defined', () => {

		expect(ContestService).toBeDefined();

	});

	describe('mapContestDetails()', () => {

		it('maps the lists and candidates details of a contest', () => {

			spyOn(CandidateService, 'mapDetailsToModel');
			spyOn(ListService, 'mapDetailsToModel');

			ContestService.mapContestDetails(contest);

			expect(ListService.mapDetailsToModel.calls.count())
				.toEqual(contest.lists.length);

			expect(CandidateService.mapDetailsToModel.calls.count())
				.toEqual(_.flatten(contest.lists.map(l => l.candidates)).length);

		});

	});

	describe('isType()', () => {

		it('returns true for if contest matches the parameter', () => {

			let isType;
			let result;

			// Candidates only contest
			contest = angular.copy(candidatesOnlyContestInitialState);
			isType = ContestService.isType.bind(contest);

			result = isType(contestTypes.CANDIDATES_ONLY);

			expect(result).toBe(true);

			contest = angular.copy(lcContestInitialState);
			isType = ContestService.isType.bind(contest);

			result = isType(contestTypes.LISTS_AND_CANDIDATES);

			expect(result).toBe(true);

			contest = angular.copy(optionsContestInitialState);
			isType = ContestService.isType.bind(contest);

			result = isType(contestTypes.OPTIONS);

			expect(result).toBe(true);

		});

	});

	describe('selectCandidate()', () => {

		it('returns false if provided parameters are invalid', () => {

			const invalidCandidates = [null, 1, {}];
			const invalidPositions = [null, -1, {}, [2]];

			invalidCandidates.forEach((candidate) => {

				const actionSucceeded = ContestService.selectCandidate(candidate, 1);

				expect(actionSucceeded).toBe(false);

			});

			invalidPositions.forEach((position) => {

				const actionSucceeded = ContestService
					.selectCandidate(contest.lists[1].candidates[0], position);

				expect(actionSucceeded).toBe(false);

			});

		});

		it('selects a candidate', () => {

			const position = 0;
			const candidate1 = contest.lists[1].candidates[0];
			const candidate2 = contest.lists[1].candidates[1];

			expect(candidate1.chosen).toBe(0);
			expect(candidate2.chosen).toBe(0);

			ContestService.selectCandidate(candidate1, position);

			expect(candidate1.chosen).toBe(1);
			expect(candidate2.chosen).toBe(0);

			expect(contest.selectedCandidates[position]).toEqual(candidate1);

		});

		it('replaces the previously selected candidate with the current selected candidate', () => {

			const position = 0;
			const candidate1 = contest.lists[1].candidates[0];
			const candidate2 = contest.lists[1].candidates[1];

			ContestService.selectCandidate(candidate1, position);

			ContestService.selectCandidate(candidate2, position);

			expect(candidate1.chosen).toBe(0);
			expect(candidate2.chosen).toBe(1);

			expect(contest.selectedCandidates[position]).toEqual(candidate2);

		});

	});

	describe('addBlankCandidate()', () => {

		let getAvailableBlankCandidateSpy;
		let selectCandidateSpy;

		beforeEach(() => {

			getAvailableBlankCandidateSpy = spyOn(ContestService, '_getAvailableBlankCandidate');
			selectCandidateSpy = spyOn(ContestService, 'selectCandidate');

		});

		it('does not add to selectedCandidates the blank candidate if no blank candidate was found', () => {

			getAvailableBlankCandidateSpy.and.returnValue(null);

			ContestService.addBlankCandidate({}, 0);

			expect(selectCandidateSpy).not.toHaveBeenCalled();

		});

		it('adds to selectedCandidates the blank candidate for the required position', () => {

			getAvailableBlankCandidateSpy.and.returnValue({});

			ContestService.addBlankCandidate({}, 0);

			expect(selectCandidateSpy).toHaveBeenCalledWith({}, 0);

		});

	});

	describe('addWriteInCandidate()', () => {

		let getAvailableWriteInCandidateSpy;
		let selectCandidateSpy;

		beforeEach(() => {

			getAvailableWriteInCandidateSpy = spyOn(ContestService, '_getAvailableWriteInCandidate');
			selectCandidateSpy = spyOn(ContestService, 'selectCandidate');

		});

		it('does not add to selectedCandidates the writein candidate if no writein candidate was found', () => {

			getAvailableWriteInCandidateSpy.and.returnValue(null);

			ContestService.addWriteInCandidate({}, 0);

			expect(selectCandidateSpy).not.toHaveBeenCalled();

		});

		it('adds to selectedCandidates the writein candidate for the required position', () => {

			getAvailableWriteInCandidateSpy.and.returnValue({});

			ContestService.addWriteInCandidate({}, 0, 'SOME_NAME');

			expect(selectCandidateSpy).toHaveBeenCalledWith({name: 'SOME_NAME'}, 0);

		});

	});

	describe('clearCandidate()', () => {

		beforeEach(() => {

			contest = angular.copy(candidatesOnlyContestInitialState);
			addParentsReferences(contest);

		});

		it('does nothing if no candidate is passed or not chosen', () => {

			spyOn(angular, 'noop');

			ContestService.clearCandidate(null);
			ContestService.clearCandidate({chosen: 0});

			expect(angular.noop.calls.count()).toBe(2);

		});

		it('subtracts one from chosen property of the selected candidate and removes it from selectedCandidates array', () => {

			const nonBlankList = contest.lists.find(l => !l.isBlank);
			const selectedCandidate = nonBlankList.candidates[0];

			selectedCandidate.chosen = 1;
			contest.selectedCandidates = [selectedCandidate];

			ContestService.clearCandidate(selectedCandidate, 0);

			expect(selectedCandidate.chosen).toBe(0);
			expect(contest.selectedCandidates).not.toContain(selectedCandidate);

		});

		it('clears the name of the candidate if candidate is a writein representation', () => {

			const blankList = contest.lists.find(l => l.isBlank);
			const selectedWriteInCandidate = blankList.candidates[0];

			selectedWriteInCandidate.isWriteIn = true;
			selectedWriteInCandidate.chosen = 1;
			selectedWriteInCandidate.name = 'writein';
			contest.selectedCandidates = [selectedWriteInCandidate];

			ContestService.clearCandidate(selectedWriteInCandidate, 0);

			expect(selectedWriteInCandidate.chosen).toBe(0);
			expect(selectedWriteInCandidate.name).toBe('');
			expect(contest.selectedCandidates).not.toContain(selectedWriteInCandidate);

		});

	});

	describe('clearCandidates()', () => {

		it('calls clearCandidate for each selected candidate', () => {

			spyOn(ContestService, 'clearCandidate');
			contest.selectedCandidates = [1, 2, 3];

			ContestService.clearCandidates(contest);

			expect(ContestService.clearCandidate.calls.count()).toBe(3);

		});

	});

	describe('_getAvailableWriteInCandidate()', () => {

		it('returns null if no write in candidates found', () => {

			spyOn(ContestService, '_getListCandidates').and.returnValue([]);

			const writeInCandidate = ContestService._getAvailableBlankCandidate({candidatesQuestion: {}}, 1);

			expect(writeInCandidate).toBeUndefined();

		});

		it('returns the writein candidate for the requested position if the candidatesQuestion.maxChoices is equal to the number of writein candidates available', () => {

			const candidates = [
				{
					id: 1,
					chosen: 0,
					isWriteIn: true,
				},
				{
					id: 2,
					chosen: 0,
					isBlank: true,
				},
				{
					id: 3,
					chosen: 0,
					isWriteIn: true,
				},
			];

			spyOn(ContestService, '_getListCandidates').and.returnValue(candidates);

			const writeInCandidate = ContestService._getAvailableWriteInCandidate(
				{candidatesQuestion: {maxChoices: 2}},
				1
			);

			expect(writeInCandidate).toEqual(candidates[2]);

		});

		it('returns the first found writein candidate if the candidatesQuestion.maxChoices is different than the number of writein candidates available', () => {

			const candidates = [
				{
					id: 1,
					chosen: 0,
					isWriteIn: true,
				},
				{
					id: 2,
					chosen: 0,
					isWriteIn: true,
				},
			];

			spyOn(ContestService, '_getListCandidates').and.returnValue(candidates);

			let writeInCandidate = ContestService._getAvailableWriteInCandidate(
				{candidatesQuestion: {maxChoices: 3}},
				1
			);

			expect(writeInCandidate).toEqual(candidates[0]);

			candidates[0].chosen = 1;

			writeInCandidate = ContestService._getAvailableWriteInCandidate(
				{candidatesQuestion: {maxChoices: 3}},
				2
			);

			expect(writeInCandidate).toEqual(candidates[1]);

		});

	});

	describe('_getAvailableBlankCandidate()', () => {

		it('returns null if no blank list found', () => {

			spyOn(ContestService, '_getListCandidates').and.returnValue([]);

			const blankCandidate = ContestService._getAvailableBlankCandidate({candidatesQuestion: {}}, 1);

			expect(blankCandidate).toBeUndefined();

		});

		it('returns the blank candidate for the requested position if the candidatesQuestion.maxChoices is equal to the number of blank candidates available', () => {

			const candidates = [
				{
					id: 1,
					chosen: 0,
					isBlank: true,
				},
				{
					id: 3,
					chosen: 0,
					isWriteIn: true,
				},
				{
					id: 2,
					chosen: 0,
					isBlank: true,
				},
			];

			spyOn(ContestService, '_getListCandidates').and.returnValue(candidates);

			const blankCandidate = ContestService._getAvailableBlankCandidate(
				{candidatesQuestion: {maxChoices: 2}},
				1
			);

			expect(blankCandidate).toEqual(candidates[2]);

		});

		it('returns the first found blank candidate if the candidatesQuestion.maxChoices is different than the number of blank candidates available', () => {

			const candidates = [
				{
					id: 1,
					chosen: 0,
					isBlank: true,
				},
				{
					id: 2,
					chosen: 0,
					isBlank: true,
				},
			];

			spyOn(ContestService, '_getListCandidates').and.returnValue(candidates);

			let blankCandidate = ContestService._getAvailableBlankCandidate(
				{candidatesQuestion: {maxChoices: 1}},
				3
			);

			expect(blankCandidate).toEqual(candidates[0]);

			candidates[0].chosen = 1;

			blankCandidate = ContestService._getAvailableBlankCandidate(
				{candidatesQuestion: {maxChoices: 3}},
				3
			);

			expect(blankCandidate).toEqual(candidates[1]);

		});

	});

	describe('_getListCandidates()', function () {

		it('returns an empty list if the provided list has no candidates', () => {

			const lists = [null, 123, {}, {candidates: null}, {candidates: []}, {candidates: 1}];

			lists.forEach((list) => {

				const result = ContestService._getListCandidates(list);

				expect(result).toEqual([]);

			});

		});

		it('returns the list of candidates for a specific list', () => {

			const list = {
				candidates: ['candidate1', 'candidate2'],
			};

			const result = ContestService._getListCandidates(list);

			expect(result).toEqual(list.candidates);

		});

	});

	describe('extractVotedOptionsRepresentations()', () => {

		it('returns the blank representation for an options contest without selection', () => {

			contest = angular.copy(optionsContestInitialState);

			const blankOption = contest.questions[0].blankOption;

			const representations = ContestService.extractVotedOptionsRepresentations(contest);

			expect(representations).toEqual({
				voteOptions: [{
					id: contest.questions[0].id,
					prime: blankOption.prime,
				}],
				writeIns: [],
			});

		});

		it('returns the selected representation for an options contest', () => {

			contest = angular.copy(optionsContestInitialState);

			const selectedOption = contest.questions[0].options[0];

			contest.questions[0].chosen = selectedOption.id;

			const representations = ContestService.extractVotedOptionsRepresentations(contest);

			expect(representations).toEqual({
				voteOptions: [{
					id: contest.questions[0].id,
					prime: selectedOption.prime,
				}],
				writeIns: [],
			});

		});

		it('returns the empty representations for a lists and candidates contest without selections', () => {

			contest = angular.copy(lcContestInitialState);

			const representations = ContestService.extractVotedOptionsRepresentations(contest);

			expect(representations).toEqual({
				voteOptions: [],
				writeIns: [],
			});

		});

		it('returns the selected representations for a candidates contest', () => {

			contest = angular.copy(candidatesOnlyContestInitialState);
			ListsAndCandidatesService.initialize(contest);

			const selectedCandidate = contest.lists.find(l => !l.isBlank).candidates[0];

			contest.selectedCandidates.push(selectedCandidate);

			const representations = ContestService.extractVotedOptionsRepresentations(contest);

			expect(representations).toEqual({
				voteOptions: [{
					id: selectedCandidate.id,
					prime: selectedCandidate.prime,
				}],
				writeIns: [],
			});

		});

		it('returns the selected representations for a lists and candidates contest', () => {

			contest = angular.copy(lcContestInitialState);
			ListsAndCandidatesService.initialize(contest);

			const selectedList = contest.lists.find(l => !l.isBlank);
			const selectedCandidate = selectedList.candidates[0];

			contest.selectedLists.push(selectedList);
			contest.selectedCandidates.push(selectedCandidate);

			const representations = ContestService.extractVotedOptionsRepresentations(contest);

			expect(representations).toEqual({
				voteOptions: [
					{
						id: selectedList.id,
						prime: selectedList.prime,
					},
					{
						id: selectedCandidate.id,
						prime: selectedCandidate.prime,
					},
				],
				writeIns: [],
			});

		});

		it('returns the selected representations and writeins for a lists and candidates contest', () => {

			contest = angular.copy(lcContestInitialState);
			ListsAndCandidatesService.initialize(contest);

			const selectedCandidate = contest.lists.find(l => !l.isBlank).candidates[0];

			selectedCandidate.isWriteIn = true;
			selectedCandidate.name = 'hello';

			contest.selectedCandidates.push(selectedCandidate);

			const representations = ContestService.extractVotedOptionsRepresentations(contest);

			expect(representations).toEqual({
				voteOptions: [{
					id: selectedCandidate.id,
					prime: selectedCandidate.prime,
				}],
				writeIns: [selectedCandidate.prime + '#' + selectedCandidate.name],
			});

		});

		it('returns the unique representations of a cumulated candidate for a lists and candidates contest', () => {

			spyOn(console, 'warn');

			contest = angular.copy(lcContestInitialState);
			ListsAndCandidatesService.initialize(contest);

			const selectedCandidate = contest.lists.find(l => !l.isBlank).candidates[0];

			selectedCandidate.allIds = ['1', '2'];
			selectedCandidate.allRepresentations = ['r1', 'r2'];

			contest.selectedCandidates.push(selectedCandidate);
			contest.selectedCandidates.push(selectedCandidate);
			contest.selectedCandidates.push(selectedCandidate);

			const representations = ContestService.extractVotedOptionsRepresentations(contest);

			expect(representations).toEqual({
				voteOptions: [
					{
						id: selectedCandidate.allIds[0],
						prime: selectedCandidate.allRepresentations[0],
					},
					{
						id: selectedCandidate.allIds[1],
						prime: selectedCandidate.allRepresentations[1],
					},
					null,
				],
				writeIns: [],
			});

			expect(console.warn).toHaveBeenCalled();
			expect(console.warn.calls.argsFor(0)[0]).toContain(selectedCandidate.id);

		});

		it('warns if a candidate was cumulated more than the number of existing unique representations', () => {

			contest = angular.copy(candidatesOnlyContestInitialState);
			ListsAndCandidatesService.initialize(contest);

			const selectedCandidate = contest.lists.find(l => !l.isBlank).candidates[0];

			selectedCandidate.allIds = ['1', '2'];
			selectedCandidate.allRepresentations = ['r1', 'r2'];

			contest.selectedCandidates.push(selectedCandidate);
			contest.selectedCandidates.push(selectedCandidate);

			const representations = ContestService.extractVotedOptionsRepresentations(contest);

			expect(representations).toEqual({
				voteOptions: [
					{
						id: selectedCandidate.allIds[0],
						prime: selectedCandidate.allRepresentations[0],
					},
					{
						id: selectedCandidate.allIds[1],
						prime: selectedCandidate.allRepresentations[1],
					},
				],
				writeIns: [],
			});

		});

	});

	describe('getPrimes()', () => {

		it('returns an empty array if provided voteOptions are invalid', () => {

			const invalidVotingOptions = [null, 1, {}, []];

			invalidVotingOptions.forEach((vo) => {

				const primes = ContestService.getPrimes(vo);

				expect(primes).toEqual([]);

			});

		});

		it('maps only those voteOptions that are having a prime property', () => {

			const votingOptions = [
				{prime: 1},
				{prime: 2},
				{notAPrimeProp: 3},
			];

			const primes = ContestService.getPrimes(votingOptions);

			expect(primes).toEqual([1, 2]);

		});

	});

});
