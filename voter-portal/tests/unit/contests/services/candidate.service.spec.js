/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

describe('Candidate Service', () => {

	let CandidateService;
	let contest;

	const contestInitialState = require('../mocks/lists-and-candidates.json');
	const {addParentsReferences} = require('../util.js');

	beforeEach(() => {

		angular.mock.module('app.services');
		angular.mock.module('app.contests');

	});

	beforeEach(inject((_CandidateService_) => {

		CandidateService = _CandidateService_;

		contest = angular.copy(contestInitialState);

		addParentsReferences(contest);

	}));

	it('should be defined', () => {

		expect(CandidateService).toBeDefined();

	});

	describe('mapDetailsToModel()', () => {

		it('does not modify the candidate if the passed candidate has no details', () => {

			const candidate = {};

			CandidateService.mapDetailsToModel(candidate);
			expect(candidate).toEqual({});

		});

		it('maps the candidate name', () => {

			const candidate = {
				details: {
					'candidateType_attribute1': 'some name',
				},
			};

			CandidateService.mapDetailsToModel(candidate);
			expect(candidate.name).toBe(candidate.details.candidateType_attribute1);

		});

	});

	describe('getAllCandidates()', () => {

		it('should return an empty array if no contest provided or contest has no list', () => {

			const noCandidatesContests = [null, 123, {}, [], {lists: [{}]}];

			noCandidatesContests.forEach((c) => {

				const result = CandidateService.getAllCandidates(c);

				expect(result).toEqual([]);

			});

		});

		it('should get all non blank candidates', () => {

			const result = CandidateService.getAllCandidates(contest);
			const expectedCandidates = _.chain(contest.lists)
				.filter(l => !l.isBlank)
				.flatMap(o => o.candidates)
				.value();

			expect(result).toEqual(expectedCandidates);

		});

	});

	describe('getCandidatePosition()', () => {

		const mockContest = {
			candidatesQuestion: {},
			lists: [
				{
					candidates: [
						{id: 1},
						{id: 2},
					],
				},
			],
			selectedCandidates: [],
		};

		it('returns -1 if contest has no selected candidates', () => {

			const position = CandidateService.getCandidatePosition(mockContest, {});

			expect(position).toEqual(-1);

		});

		it('returns the position of the passed candidate', () => {

			mockContest.selectedCandidates.push(mockContest.lists[0].candidates[1]);

			let result = CandidateService.getCandidatePosition(
				mockContest,
				mockContest.lists[0].candidates[0]
			);

			expect(result).toEqual(-1);

			result = CandidateService.getCandidatePosition(
				mockContest,
				mockContest.lists[0].candidates[1]
			);

			expect(result).toEqual(0);

			mockContest.selectedCandidates.push(mockContest.lists[0].candidates[0]);

			result = CandidateService.getCandidatePosition(
				mockContest,
				mockContest.lists[0].candidates[0]
			);

			expect(result).toEqual(1);

		});

	});

	describe('isCandidateSelected()', () => {

		it('returns false if no candidate provided or candidate has no chosen property', () => {

			const invalidCandidates = [null, 1, {}, {chosen: '0'}, {
				chosen: () => {
					// Empty function
				}
			}];

			invalidCandidates.forEach((candidate) => {

				const result = CandidateService.isCandidateSelected(candidate);

				expect(result).toBe(false);

			});


		});

		it('returns if candidate is selected', () => {

			let result = CandidateService.isCandidateSelected({chosen: 0});

			expect(result).toEqual(false);

			result = CandidateService.isCandidateSelected({chosen: 2});

			expect(result).toEqual(true);

		});

	});

	describe('getCandidateCumul()', () => {

		it('returns 0 if no candidate was passed or there are no selected candidates', () => {

			const invalidCandidates = [null, 1, {}];

			invalidCandidates.forEach((candidate) => {

				const cumul = CandidateService.getCandidateCumul(candidate);

				expect(cumul).toBe(0);

			});

		});

		it('returns the number of how many times a candidate was selected', () => {

			const mockCandidate = {
				id: 1,
				parent: { // list
					parent: { // contest
						selectedCandidates: [{id: 1}, {id: 1}, {id: 2}],
					},
				},
			};

			const cumul = CandidateService.getCandidateCumul(mockCandidate);

			expect(cumul).toBe(2);

		});

	});

	describe('candidateMaxAllowedCumul()', () => {

		it('returns 1 if no candidate was specified or the candidate has no representations', () => {

			const invalidCandidates = [null, 1, {}];

			invalidCandidates.forEach((candidate) => {

				const maxCumul = CandidateService.candidateMaxAllowedCumul(candidate);

				expect(maxCumul).toBe(1);

			});

		});

		it('returns the maximum allowed cumul based on the number of representations the candidate has', () => {

			const candidate = {
				allRepresentations: ['1', '2'],
			};

			const maxCumul = CandidateService.candidateMaxAllowedCumul(candidate);

			expect(maxCumul).toBe(2);

		});

	});

	describe('isWriteInValid()', () => {

		const writeInAlphabet = '# ()0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ`abcdefghijklmnopqrstuvwxyz ¡¢ŠšŽžŒœŸÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ';

		it('returns true if passed candidate is not a write-in or no name was specified', () => {

			const invalidCandidates = [null, 1, {}, {isWriteIn: true}, {name: 'some name'}];

			invalidCandidates.forEach((candidate) => {

				const result = CandidateService.isWriteInValid(candidate);

				expect(result).toBe(true);

			});

		});

		it('returns true if the name of a writein candidate uses only valid characters', () => {

			const writeInCandidate = {
				isWriteIn: true,
				name: 's0me ValiÐ Ç4aractærs',
				parent: {
					parent: {
						parent: {
							writeInAlphabet,
						},
					},
				},
			};

			const result = CandidateService.isWriteInValid(writeInCandidate);

			expect(result).toBe(true);

		});

		it('returns true if the no writein alphabet is present in the ballot', () => {

			const writeInCandidate = {
				isWriteIn: true,
				name: 'valid name',
			};

			const result = CandidateService.isWriteInValid(writeInCandidate);

			expect(result).toBe(true);

		});

		it('returns "alphabet" if the name of a writein candidate uses invalid characters', () => {

			const writeInCandidate = {
				isWriteIn: true,
				name: 'inv@lid name',
				parent: {
					parent: {
						parent: {
							writeInAlphabet,
						},
					},
				},
			};

			const result = CandidateService.isWriteInValid(writeInCandidate);

			expect(result).toEqual('alphabet');

		});

		it('returns "name" if the name of a writein candidate does not have a "firstname lastname" format', () => {

			const writeInCandidate = {
				isWriteIn: true,
				name: 'invalidname',
				parent: {
					parent: {
						parent: {
							writeInAlphabet,
						},
					},
				},
			};

			const result = CandidateService.isWriteInValid(writeInCandidate);

			expect(result).toEqual('name');

		});

	});

	describe('isAliasSelected()', () => {

		it('returns false if no candidate was specified or is of an invalid form', () => {

			const invalidCandidates = [null, 1, {}];

			invalidCandidates.forEach((candidate) => {

				const isAliasSelected = CandidateService.isAliasSelected(candidate);

				expect(isAliasSelected).toBe(false);

			});

		});

		it('returns false if the contest has no fusions specified', () => {

			const mockCandidate = {
				alias: 'a1',
				parent: { // list
					parent: {
						candidatesQuestion: {},
						selectedCandidates: [{alias: 'a2'}],
					}, // contest
				},
			};
			const invalidFusions = [null, 1, []];

			invalidFusions.forEach((invalidFusion) => {

				const candidate = mockCandidate;

				candidate.parent.parent.candidatesQuestion.fusions = invalidFusion;

				const isAliasSelected = CandidateService.isAliasSelected(candidate);

				expect(isAliasSelected).toBe(false);

			});

		});

		it('returns true if the candidate\'s alias is found in one of the selected candidates', () => {

			const mockCandidate = {
				id: 1,
				alias: 'a1',
				parent: { // list
					parent: { // contest
						candidatesQuestion: {
							fusions: [['a1', 'a2', 'a3']],
						},
						selectedCandidates: [{id: 2, alias: 'a2'}],
					},
				},
			};

			const isAliasSelected = CandidateService.isAliasSelected(mockCandidate);

			expect(isAliasSelected).toBe(true);

		});

	});

});
