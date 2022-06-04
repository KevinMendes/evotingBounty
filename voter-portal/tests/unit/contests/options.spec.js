/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../helpers');

describe('Options component', () => {

	let $rootScope;
	let OptionsService;
	let contest;
	let elementUT;
	let scope;

	const contestInitialState = require('./mocks/options.json');

	const directive = '<options data-ng-model="contest"></options>';

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_, _OptionsService_) => {

		$rootScope = _$rootScope_;
		OptionsService = _OptionsService_;

		contest = angular.copy(contestInitialState);

		scope = $rootScope.$new();
		scope.contest = contest;

		elementUT = getCompiledDirective(directive, scope);

	}));

	it('should display the options contest type', () => {

		scope.contest = contest;

		elementUT = getCompiledDirective(directive, scope);

		const container = elementUT[0].getElementsByClassName('ballot-options')[0];

		expect(container).toBeDefined();

	});

	it('OptionsService should be initialized', () => {

		spyOn(OptionsService, 'initialize');

		scope = $rootScope.$new();

		scope.contest = contest;

		getCompiledDirective(directive, scope);

		expect(OptionsService.initialize)
			.toHaveBeenCalledWith(contest);

	});

});
