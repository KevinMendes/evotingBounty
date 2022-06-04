/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../../helpers');

describe('list-finder component', () => {

	let $rootScope;

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_) => {

		$rootScope = _$rootScope_;

	}));

	it('should have the search text filled in when data is passed as attribute', () => {

		const scope = $rootScope.$new();

		scope.searchData = {
			searchPartyText: 'some party',
		};

		const directive = '<list-finder data="searchData">';

		const elementUT = getCompiledDirective(directive, scope)[0];

		expect(elementUT).toBeDefined();

		const searchValue = elementUT.querySelector('#fc_search');

		expect(searchValue.value).toBe(scope.searchData.searchPartyText);

	});

	it('clears the search when clicked on button', () => {

		const scope = $rootScope.$new();

		scope.searchData = {
			searchPartyText: 'some party',
		};

		const directive = '<list-finder data="searchData">';

		const elementUT = getCompiledDirective(directive, scope)[0];

		elementUT.querySelector('#btn_clear_list').click();

		const searchValue = elementUT.querySelector('#fc_search');

		expect(searchValue.value).toBe('');

	});

});
