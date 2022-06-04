/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../../helpers');

const SOME_CANDIDATE_STRING = 'some candidate';

describe('candidate-finder component', () => {

	let $rootScope;
	let scope;

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	const directive = getCompiledDirective('<candidate-finder data="data">');

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_) => {

		$rootScope = _$rootScope_;
		scope = $rootScope.$new();

	}));

	/**
	 * Controller
	 */
	xdescribe('searchCandidate()', () => {

		let searchService;
		let searchSpy;

		beforeEach(inject((_searchService_) => {

			searchService = _searchService_;
			searchSpy = spyOn(searchService, 'search');

		}));


		it('calls searchService', () => {

			scope.data = {
				allCandidates: [1, 2, 3],
				searchCandidateText: SOME_CANDIDATE_STRING,
			};

			searchSpy.and.returnValue([2, 3]);

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.searchCandidate();

			expect(isolatedScope.data.searchedText).toEqual(scope.data.searchCandidateText);

			expect(searchSpy).toHaveBeenCalledWith(
				isolatedScope.data.searchCandidateText,
				'name',
				isolatedScope.data.allCandidates
			);

			expect(isolatedScope.data.candidates).toEqual([2, 3]);


		});

	});

	xdescribe('filterByListById()', () => {

		let CandidateService;
		let getAllCandidatesSpy;

		beforeEach(inject((_CandidateService_) => {

			CandidateService = _CandidateService_;

			getAllCandidatesSpy = spyOn(CandidateService, 'getAllCandidates');

		}));

		it('shows all candidates of the provided list id', () => {

			const allLists = [
				{id: 1, candidates: [1, 2]},
				{id: 2, candidates: [3, 4]},
			];

			scope.data = {
				allLists,
			};

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.filterByListById(1);

			expect(isolatedScope.data.allCandidates).toEqual(allLists[0].candidates);

			expect(isolatedScope.data.candidates).toEqual(allLists[0].candidates);

		});

		it('shows all candidates of all lists if no id was provided', () => {

			const allCandidates = [1, 2, 3, 4];

			scope.data = {
				allLists: [],
			};

			getAllCandidatesSpy.and.returnValue(allCandidates);

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.filterByListById();


			expect(getAllCandidatesSpy.calls.count()).toBe(2);

			expect(isolatedScope.data.allCandidates).toEqual(allCandidates);

			expect(isolatedScope.data.candidates).toEqual(allCandidates);

		});

		it('calls searchCandidate if searchCandidateText is defined', () => {

			scope.data = {
				allLists: [],
				searchCandidateText: SOME_CANDIDATE_STRING,
			};

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			spyOn(isolatedScope, 'searchCandidate');

			isolatedScope.filterByListById();

			expect(isolatedScope.searchCandidate).toHaveBeenCalled();

		});

	});

	xdescribe('clearCandidate()', () => {

		it('clears appropriate scope variable and calls searchCandidate', () => {

			scope.data = {
				searchCandidateText: SOME_CANDIDATE_STRING,
			};

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			spyOn(isolatedScope, 'searchCandidate');

			isolatedScope.clearCandidate();

			expect(scope.data.searchCandidateText).toEqual('');
			expect(isolatedScope.searchCandidate).toHaveBeenCalled();

		});

	});

	xdescribe('showOnlySelectedFilter()', () => {

		it('returns always true if showOnlySelected is false', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.showOnlySelected = false;

			const result = isolatedScope.showOnlySelectedFilter()({chosen: 0});

			expect(result).toBe(true);

		});

		it('returns true if showOnlySelected is true and item chosen property is grater than 0', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.showOnlySelected = true;

			const result = isolatedScope.showOnlySelectedFilter()({chosen: 1});

			expect(result).toBe(true);

		});

		it('returns false if showOnlySelected is true and item chosen property is 0', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.showOnlySelected = true;

			const result = isolatedScope.showOnlySelectedFilter()({chosen: 0});

			expect(result).toBe(false);

		});

	});

	xit('should have the search text filled in when data is passed as attribute', () => {

		scope.data = {
			searchCandidateText: SOME_CANDIDATE_STRING,
		};

		const elementUT = directive(scope)[0];

		expect(elementUT).toBeDefined();

		const searchValue = elementUT.querySelector('#fc_search');

		expect(searchValue.value).toBe(scope.data.searchCandidateText);

	});

	xit('clears the search when clicked on button', () => {

		scope.data = {
			searchCandidateText: SOME_CANDIDATE_STRING,
		};

		const elementUT = directive(scope)[0];

		elementUT.querySelector('#btn_clear_candidate').click();

		const searchValue = elementUT.querySelector('#fc_search');

		expect(searchValue.value).toBe('');

	});

});
