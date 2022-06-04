/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../../helpers');

describe('Contest component', () => {

	let $rootScope;
	let gettextCatalog;
	let scope;

	const contestInitialState = require('../mocks/lists-and-candidates.json');

	const directive = getCompiledDirective('<div contest-chooser contest="contest"></div>');

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_, _gettextCatalog_) => {

		$rootScope = _$rootScope_;
		gettextCatalog = _gettextCatalog_;

		scope = $rootScope.$new();
		scope.contest = angular.copy(contestInitialState);

	}));

	/**
	 * Controller
	 */
	describe('getSelectedCandidatesCount()', () => {

		it('returns 0 if contest selectedCandidate is null', () => {

			scope.contest.selectedCandidates = null;

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			const result = isolatedScope.getSelectedCandidatesCount();

			expect(result).toBe(0);

		});

		it('returns 0 if contest has no selected candidates', () => {

			scope.contest.selectedCandidates = [];

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			const result = isolatedScope.getSelectedCandidatesCount();

			expect(result).toBe(0);

		});

		it('returns the count of real representations of candidates exist in the selectedCandidates prop excluding blanks', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			scope.contest.selectedCandidates = [null, {}, {isBlank: false}, {isBlank: true}];

			const result = isolatedScope.getSelectedCandidatesCount();

			expect(result).toBe(2);

		});

	});

	it('should display the contest component', () => {

		const elementUT = directive(scope);

		const title = elementUT[0].getElementsByClassName('ballot-group-title')[0];

		expect(title.innerText)
			.toContain(
				gettextCatalog.getString(
					'01 Lists And Candidates Event'
				)
			);

	});

	it('should be expanded on init', () => {

		const elementUT = directive(scope);

		const collapsible = elementUT[0].getElementsByClassName('collapsible-contest')[0];

		expect(angular.element(collapsible).hasClass('is-closed')).toBe(false);

	});

	it('should collapse / expand on click', () => {

		const elementUT = directive(scope);

		const collapsible = elementUT[0].getElementsByClassName('collapsible-contest')[0];

		expect(angular.element(collapsible).hasClass('is-closed')).toBe(false);

		const collapsibler = elementUT[0].getElementsByClassName('collapsibler')[0];

		angular.element(collapsibler).triggerHandler('click');

		expect(angular.element(collapsible).hasClass('is-closed')).toBe(true);

	});

});
