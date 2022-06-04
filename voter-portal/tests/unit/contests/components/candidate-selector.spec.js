/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../../helpers');

describe('candidate-selector component', () => {

	let CandidateService;
	let $rootScope;
	let scope;

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	const directive = getCompiledDirective('<candidate-selector candidate="candidate" position="position" on-selected="onCandidateSelected">');

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_, _CandidateService_) => {

		$rootScope = _$rootScope_;
		CandidateService = _CandidateService_;

		scope = $rootScope.$new();

		scope.candidate = {};
		scope.position = 1;
		scope.onCandidateSelected = jasmine.createSpy('onCandidateSelected');

	}));

	/**
	 * Controller
	 */
	describe('candidateMaxAllowedCumul()', () => {

		it('calls CandidateService.candidateMaxAllowedCumul', () => {

			spyOn(CandidateService, 'candidateMaxAllowedCumul');

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.candidateMaxAllowedCumul();

			expect(CandidateService.candidateMaxAllowedCumul)
				.toHaveBeenCalledWith(scope.candidate);

		});

	});


	describe('cumulAllowed()', () => {

		it('returns true if candidateMaxAllowedCumul is lower or equally to 1', () => {

			spyOn(CandidateService, 'candidateMaxAllowedCumul').and.returnValue(1);

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			const result = isolatedScope.cumulAllowed();

			expect(result).toBe(false);

		});

		it('returns true if candidateMaxAllowedCumul is greater than 1', () => {

			spyOn(CandidateService, 'candidateMaxAllowedCumul').and.returnValue(2);

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			const result = isolatedScope.cumulAllowed();

			expect(result).toBe(true);

		});

	});

	describe('selectCandidate()', () => {

		it('does nothing if onSelected is not defined', () => {

			spyOn(angular, 'noop');
			scope.onCandidateSelected = null;

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.selectCandidate();

			expect(angular.noop)
				.toHaveBeenCalled();

		});

		it('calls outer\'s scope onSelected if defined', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.selectCandidate();

			expect(scope.onCandidateSelected)
				.toHaveBeenCalledWith({
					candidate: scope.candidate,
					position: scope.position,
				});

		});

	});

});
