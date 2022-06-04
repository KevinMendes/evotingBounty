/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../../helpers');

describe('options-selector component', () => {

	let $rootScope;
	let scope;

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	const directive = getCompiledDirective('<option-selector option="question">');

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_) => {

		$rootScope = _$rootScope_;
		scope = $rootScope.$new();

		scope.question = {};

	}));

	/**
	 * Controller
	 */
	describe('resetRadio()', () => {

		it('removes the chosen option from the question', () => {

			scope.question.chosen = '1';

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.resetRadio();

			expect(scope.question.chosen)
				.toBe('');

		});

	});

});
