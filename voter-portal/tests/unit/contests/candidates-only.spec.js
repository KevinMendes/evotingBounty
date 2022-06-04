/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');
const {getCompiledDirective} = require('../helpers');

describe('candidates-only component', () => {

	let $rootScope;
	let ContestService;
	let CandidatesOnlyService;
	let contest;
	let elementUT;
	let scope;

	const contestInitialState = require('./mocks/candidates-only-1.json');

	const directive = '<candidates-only data-ng-model="contest"></candidates-only>';

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	/**
	 * Inject dependencies
	 */
	beforeEach(inject(
		(_$rootScope_, _ContestService_, _CandidatesOnlyService_) => {

			$rootScope = _$rootScope_;
			ContestService = _ContestService_;
			CandidatesOnlyService = _CandidatesOnlyService_;

			contest = angular.copy(contestInitialState);

			spyOn(CandidatesOnlyService, 'initialize');
			spyOn(CandidatesOnlyService, 'addSelectedListToContest');
			spyOn(CandidatesOnlyService, 'clearList');
			spyOn(ContestService, 'selectCandidate');
			spyOn(ContestService, 'clearCandidate');

			scope = $rootScope.$new();
			scope.contest = contest;

			elementUT = getCompiledDirective(directive, scope);

		}
	));

	/**
	 * UI Logic
	 */
	it('should display both lists and candidates preselectors for candidatesQuestion.maxChoices > 1', () => {

		scope.contest.candidatesQuestion.maxChoices = 3;

		elementUT = getCompiledDirective(directive, scope);

		const container = elementUT[0].getElementsByClassName('candidates-only');

		expect(container.length).toBe(1);

		const list = elementUT[0].getElementsByClassName('ballot-election-party');

		expect(list).toBeDefined();

		const candidates = elementUT[0].getElementsByClassName('ballot-election-candidate');

		expect(candidates.length)
			.toBe(scope.contest.candidatesQuestion.maxChoices);

	});

	it('should display both lists and candidates preselectors for candidatesQuestion.maxChoices == 1', () => {

		scope.contest.candidatesQuestion.maxChoices = 1;

		elementUT = getCompiledDirective(directive, scope);

		const container = elementUT[0].getElementsByClassName('candidates-only');

		expect(container.length).toBe(1);

		const list = elementUT[0].getElementsByClassName('ballot-election-party');

		expect(list.length).toBe(0);

		const candidates = elementUT[0].getElementsByClassName('ballot-election-candidate');

		expect(candidates.length).toBe(scope.contest.candidatesQuestion.maxChoices);

	});

	/**
	 * Directive's controller logic
	 */
	it('should call CandidatesOnlyService.initialize on link phase', () => {

		expect(CandidatesOnlyService.initialize)
			.toHaveBeenCalledWith(contest);

	});

	it('should call CandidatesOnlyService.addSelectedListToContest when a list is selected', () => {

		elementUT.isolateScope().onListSelected('LIST');

		expect(CandidatesOnlyService.addSelectedListToContest)
			.toHaveBeenCalledWith('LIST');

	});

	it('should call CandidatesOnlyService.clearList when a list is cleared', () => {

		elementUT.isolateScope().onListCleared();

		expect(CandidatesOnlyService.clearList)
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
