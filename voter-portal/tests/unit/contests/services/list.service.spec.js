/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

describe('List Service', () => {

	let ListService;
	let ListsAndCandidatesService;
	let contest;

	const contestInitialState = require('../mocks/lists-and-candidates.json');
	const {addParentsReferences} = require('../util.js');

	beforeEach(() => {

		angular.mock.module('app.services');
		angular.mock.module('app.contests');

	});

	beforeEach(inject((_ListService_, _ListsAndCandidatesService_) => {

		ListService = _ListService_;
		ListsAndCandidatesService = _ListsAndCandidatesService_;

		contest = angular.copy(contestInitialState);
		addParentsReferences(contest);

		ListsAndCandidatesService.initialize(contest);

	}));

	it('should be defined', () => {

		expect(ListService).toBeDefined();

	});

	describe('mapDetailsToModel()', () => {

		it('does not modify the list if the passed list has no details', () => {

			const list = {};

			ListService.mapDetailsToModel(list);
			expect(list).toEqual({});

		});

		it('maps list name', () => {

			const list = {
				details: {
					'listType_attribute1': 'some name',
				},
			};

			ListService.mapDetailsToModel(list);
			expect(list.name).toBe(list.details.listType_attribute1);

		});

	});

	describe('getAllLists()', () => {

		it('gets all non blank lists', () => {

			const result = ListService.getAllLists(contest);

			expect(result).toEqual(contest.lists.filter(l => !l.isBlank && !l.isWriteIn));

		});

	});

	describe('getBlankList()', () => {

		it('should get the blank list', () => {

			const result = ListService.getBlankList(contest);

			expect(result).toEqual(contest.lists.find(l => l.isBlank));

		});

	});

	describe('getMaxAvailableSeats()', () => {

		it('reports an error if contest has no candidatesQuestion or no maxChoices property', () => {

			const invalidContests = [null, {}, {candidatesQuestion: {}}];

			spyOn(console, 'error');

			invalidContests.forEach((c) => {

				const result = ListService.getMaxAvailableSeats(c);

				expect(result).toEqual(0);

				expect(console.error).toHaveBeenCalled();

			});


		});

		it('gets the available slots', () => {

			const result = ListService.getMaxAvailableSeats(contest);

			expect(result).toEqual(contest.candidatesQuestion.maxChoices);

		});

	});

	describe('hasListDetails()', () => {

		it('should return false if list has no description', () => {

			const invalidLists = [null, {}, {description: null}, {description: ''}, {description: []}];

			invalidLists.forEach(list => {

				const result = ListService.hasListDetails(list);

				expect(result).toEqual(false);

			});

		});


		it('return true if list has description', () => {

			const list = {description: ['some description']};

			const result = ListService.hasListDetails(list);

			expect(result).toEqual(true);

		});

	});

});
