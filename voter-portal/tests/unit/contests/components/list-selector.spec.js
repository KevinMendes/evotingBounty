/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../../helpers');

describe('list-selector component', () => {

	let $rootScope;
	let scope;

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	const directive = getCompiledDirective('<list-selector list="list" position="position" on-selected="onListSelected">');

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_) => {

		$rootScope = _$rootScope_;

		scope = $rootScope.$new();

		scope.list = {};
		scope.onListSelected = jasmine.createSpy('onListSelected');

	}));

	/**
	 * Controller
	 */
	describe('selectParty()', () => {

		it('does nothing if onSelected is not defined', () => {

			spyOn(angular, 'noop');
			scope.onListSelected = null;

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.selectParty();

			expect(angular.noop)
				.toHaveBeenCalled();

		});

		it('calls outer\'s scope onSelected if defined', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.selectParty();

			expect(scope.onListSelected)
				.toHaveBeenCalledWith(scope.list);

		});

	});

});
