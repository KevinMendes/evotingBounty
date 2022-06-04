/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const {getCompiledDirective} = require('../../helpers');

let $rootScope;
let ListsAndCandidatesService;
let scope;

const directive = getCompiledDirective(
	`<list-pre-selector
      ng-model    = "selectedList"
      contest     = "contest"
      on-selected = "onListSelected"
      on-cleared  = "onListCleared"
    >`
);

const contestInitialState = require('../mocks/lists-and-candidates.json');

describe('list-pre-selector component', () => {

	/**
	 * Init module
	 */
	beforeEach(angular.mock.module('app.contests'));

	/**
	 * Inject dependencies
	 */
	beforeEach(inject((_$rootScope_, _ListsAndCandidatesService_) => {

		$rootScope = _$rootScope_;
		ListsAndCandidatesService = _ListsAndCandidatesService_;

		scope = $rootScope.$new();
		scope.selectedList = null;
		scope.contest = angular.copy(contestInitialState);
		scope.onListSelected = jasmine.createSpy('onListSelected');
		scope.onListCleared = jasmine.createSpy('onListCleared');

		ListsAndCandidatesService.initialize(scope.contest);

	}));

	/**
	 * Controller
	 */

	describe('openListSelectionModal()', () => {

		let $modal;
		let modalPromise;
		let modalOpenSpy;
		let getAllListsSpy;

		beforeEach(inject((_ListService_, _CandidateService_, _$modal_, _$q_) => {

			$modal = _$modal_;
			modalPromise = _$q_.defer();

			getAllListsSpy = spyOn(_ListService_, 'getAllLists');
			modalOpenSpy = spyOn($modal, 'open').and.returnValue({
				result: modalPromise.promise,
			});

		}));

		it('obtains the data needed for the modal', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.openListSelectionModal();

			expect(isolatedScope.data.searchedText).toBe('');
			expect(isolatedScope.data.searchPartyText).toBe('');

			expect(getAllListsSpy)
				.toHaveBeenCalledWith(scope.contest);

		});

		it('calls $modal.open', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.openListSelectionModal();

			expect(modalOpenSpy)
				.toHaveBeenCalledWith({
					templateUrl: 'views/modals/choose-party.tpl.html',
					controller: 'defaultModal',
					size: 'lg',
					scope: isolatedScope,
				});

		});

		it('calls onSelected method when $modal.open promise resolves', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.openListSelectionModal();

			modalPromise.resolve({});

			$rootScope.$apply();

			expect(scope.onListSelected)
				.toHaveBeenCalledWith({});

		});

		it('calls angular.noop if no onSelected method was provided for when $modal.open promise resolves', () => {

			scope.onListSelected = null;
			spyOn(angular, 'noop');

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.openListSelectionModal();

			modalPromise.resolve({});

			$rootScope.$apply();

			expect(angular.noop)
				.toHaveBeenCalled();

		});

	});

	describe('clearList()', () => {

		it('calls outer scope\'s onCleared function', () => {

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.clearList();

			expect(scope.onListCleared)
				.toHaveBeenCalled();

		});

		it('calls angular.noop if no onCleared method was provided for when $modal.open promise resolves', () => {

			scope.onListCleared = null;
			spyOn(angular, 'noop');

			const elementUT = directive(scope);
			const isolatedScope = elementUT.isolateScope();

			isolatedScope.clearList();

			expect(angular.noop)
				.toHaveBeenCalled();

		});

	});


	/**
	 * Template
	 */
	it('should contain attribute1 and attribute2 in the list', () => {

		scope.selectedList = scope.contest.lists.find(list => list.name === 'Green List');

		const elementUT = directive(scope);

		const attribute1 = elementUT[0].getElementsByClassName('list_attribute1')[0];

		expect(attribute1).toBeDefined();
		expect(attribute1.innerText).toContain(scope.selectedList.details.listType_apparentment);

		const attribute2 = elementUT[0].getElementsByClassName('list_attribute2')[0];

		expect(attribute2).toBeDefined();
		expect(attribute2.innerText).toContain(scope.selectedList.details.listType_sousApparentment);

	});

	it('should not contain neither attribute1 nor attribute2 in the list', () => {

		scope.selectedList = scope.contest.lists.find(list => list.name === 'Blue List');

		const elementUT = directive(scope);

		const attribute1 = elementUT[0].getElementsByClassName('list_attribute1')[0];

		expect(attribute1).toBeUndefined();

		const attribute2 = elementUT[0].getElementsByClassName('list_attribute2')[0];

		expect(attribute2).toBeUndefined();

	});


});
