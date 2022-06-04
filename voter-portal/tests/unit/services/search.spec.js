/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

describe('searchService', () => {

	let searchService;

	beforeEach(angular.mock.module('app.services'));

	beforeEach(inject((_searchService_) => {

		searchService = _searchService_;

	}));

	describe('search()', () => {

		const items = [
			{name: 'red'},
			{name: 'blue'},
			{name: 'green'},
		];

		it('returns the entire collection of items if no searchText param was passed', () => {

			const result = searchService.search(null, 'name', items);

			expect(result).toEqual(items);

		});

		it('returns the items that contain the passed searchText for the given property', () => {

			const result = searchService.search('red', 'name', items);

			expect(result).toEqual([items[0]]);

		});

		it('returns empty array if the items do not have the provided property', () => {

			const result = searchService.search('red', 'description', items);

			expect(result).toEqual([]);

		});

		it('returns empty array if no collection was provided', () => {

			const result = searchService.search('red', 'description');

			expect(result).toEqual([]);

		});

	});

});
