/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../helpers');

describe('ListsAndCandidates component', () => {

	let $rootScope;
	let ContestService;
	let ListsAndCandidatesService;
	let contest;
	let elementUT;
	let scope;
	let $modal;
	let modalPromise;
	let modalOpenSpy;

	const contestInitialState = require('./mocks/lists-and-candidates.json');

	const directive = '<lists-and-candidates data-ng-model="contest"></lists-and-candidates>';

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_, _ListsAndCandidatesService_, _ContestService_, _$modal_, _$q_) => {

		$rootScope = _$rootScope_;
		ListsAndCandidatesService = _ListsAndCandidatesService_;
		ContestService = _ContestService_;
		$modal = _$modal_;

		modalPromise = _$q_.defer();

		contest = angular.copy(contestInitialState);

		spyOn(ListsAndCandidatesService, 'initialize');
		spyOn(ListsAndCandidatesService, 'addSelectedListToContest');
		spyOn(ListsAndCandidatesService, 'clearList');
		spyOn(ContestService, 'selectCandidate');
		spyOn(ContestService, 'clearCandidate');
		modalOpenSpy = spyOn($modal, 'open').and.returnValue({
			result: modalPromise.promise,
		});

		scope = $rootScope.$new();
		scope.contest = contest;
		elementUT = getCompiledDirective(directive, scope);

	}));

	it('should display the lists and candidates component for candidatesQuestion.maxChoices == 3', () => {

		scope.contest.candidatesQuestion.maxChoices = 3;

		elementUT = getCompiledDirective(directive, scope);

		const container = elementUT[0].getElementsByClassName('lists-and-candidates');

		expect(container.length).toBe(1);

		const list = elementUT[0].getElementsByClassName('ballot-election-party');

		expect(list.length).toBe(1);

		const candidates = elementUT[0].getElementsByClassName('ballot-election-candidate');

		expect(candidates.length)
			.toBe(scope.contest.candidatesQuestion.maxChoices);

	});

	it('should display the lists and candidates component for candidatesQuestion.maxChoices == 1', () => {

		scope.contest.candidatesQuestion.maxChoices = 1;

		elementUT = getCompiledDirective(directive, scope);

		const container = elementUT[0].getElementsByClassName('lists-and-candidates');

		expect(container.length).toBe(1);

		const list = elementUT[0].getElementsByClassName('ballot-election-party');

		expect(list.length).toBe(1);

		const candidates = elementUT[0].getElementsByClassName('ballot-election-candidate');

		expect(candidates.length)
			.toBe(scope.contest.candidatesQuestion.maxChoices);

	});

	it('list should be empty on init', () => {

		scope.contest = contest;

		getCompiledDirective(directive, scope);

		expect(ListsAndCandidatesService.initialize)
			.toHaveBeenCalledWith(contest);

	});

	/**
	 * Directive's controller logic
	 */
	it('should call ListsAndCandidatesService.initialize on link phase', () => {

		expect(ListsAndCandidatesService.initialize)
			.toHaveBeenCalledWith(contest);

	});

	it('should call ListsAndCandidatesService.addSelectedListToContest when a list is selected', () => {

		elementUT.isolateScope().onListSelected('LIST');

		expect(ListsAndCandidatesService.addSelectedListToContest)
			.toHaveBeenCalledWith('LIST');

	});

	it('should show a confirmation modal when a list is cleared', () => {

		elementUT.isolateScope().onListCleared();

		expect(modalOpenSpy)
			.toHaveBeenCalledWith({
				templateUrl: 'views/modals/confirmModal.tpl.html',
				controller: 'confirmModal',
				resolve: {
					labels: jasmine.any(Function),
				},
			});

	});

	it('should call ListsAndCandidatesService.clearList when a list is cleared and action confirmed by modal', () => {

		elementUT.isolateScope().onListCleared();

		modalPromise.resolve({});

		$rootScope.$apply();

		expect(ListsAndCandidatesService.clearList)
			.toHaveBeenCalledWith(scope.contest);

	});

	it('should call ContestService.selectCandidate when a candidate is selected', () => {

		const selectedCandidate = {
			candidate: 'CANDIDATE',
			position: 1,
		};

		elementUT.isolateScope().onCandidateSelected(selectedCandidate);

		expect(ContestService.selectCandidate)
			.toHaveBeenCalledWith(
				selectedCandidate.candidate,
				selectedCandidate.position
			);

	});

	it('should call CandidatesOnlyService.clearCandidate when a candidate is cleared', () => {

		elementUT.isolateScope().onCandidateCleared('CANDIDATE', 1);

		expect(ContestService.clearCandidate)
			.toHaveBeenCalledWith('CANDIDATE', 1);

	});


});
