/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

describe('Options Service', () => {

	let ContestService;
	let OptionsService;
	let contest;

	const contestInitialState = require('../mocks/options.json');

	beforeEach(() => {

		angular.mock.module('app.services');
		angular.mock.module('app.contests');

	});

	beforeEach(inject((_ContestService_, _OptionsService_) => {

		ContestService = _ContestService_;
		OptionsService = _OptionsService_;

		contest = angular.copy(contestInitialState);

	}));

	it('should be defined', () => {

		expect(OptionsService).toBeDefined();

	});

});
